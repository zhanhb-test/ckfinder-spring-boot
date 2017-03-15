/*
 * CKFinder
 * ========
 * http://cksource.com/ckfinder
 * Copyright (C) 2007-2015, CKSource - Frederico Knabben. All rights reserved.
 *
 * The software, this file and its contents are subject to the CKFinder
 * License. Please read the license.txt file before using, installing, copying,
 * modifying or distribute this file or part of its contents. The contents of
 * this file is part of the Source Code of CKFinder.
 */
package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.ThumbnailArguments;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>Thumbnail</code> command. Get thumbnail for file
 * command.
 */
@Slf4j
public class ThumbnailCommand extends Command<ThumbnailArguments> {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
          .withZone(ZoneId.of("GMT"));

  public ThumbnailCommand() {
    super(ThumbnailArguments::new);
  }

  /**
   * Gets mime type of image.
   *
   * @param sc the {@code ServletContext} object.
   * @param response currect response object
   * @return mime type of the image.
   */
  private String getMimeTypeOfImage(ServletContext sc,
          HttpServletResponse response, ThumbnailArguments arguments) {
    String fileName = arguments.getFileName();
    if (fileName != null && fileName.length() != 0) {
      String fileExtension = FileUtils.getFileExtension(fileName);
      if (fileExtension != null) {
        String tempFileName = fileName.substring(0, fileName.lastIndexOf(46) + 1).concat(fileExtension.toLowerCase());
        String mimeType = sc.getMimeType(tempFileName);
        if (mimeType != null && mimeType.length() != 0) {
          return mimeType;
        }
      }
    }
    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    return null;
  }

  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(ThumbnailArguments arguments, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration) throws ConnectorException {
    validate(arguments, configuration);
    createThumb(arguments, configuration);
    response.setHeader("Cache-Control", "public");
    String mimetype = getMimeTypeOfImage(request.getServletContext(), response, arguments);
    if (mimetype != null) {
      response.setContentType(mimetype);
    }
    response.addHeader("Content-Disposition",
            ContentDisposition.getContentDisposition("attachment", arguments.getFileName()));
    if (setResponseHeadersAfterCreatingFile(response, arguments)) {
      try (ServletOutputStream out = response.getOutputStream()) {
        FileUtils.printFileContentToResponse(arguments.getThumbFile(), out);
      } catch (IOException e) {
        log.error("", e);
        try {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException e1) {
          throw new ConnectorException(e1);
        }
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
  }

  @Override
  protected void initParams(ThumbnailArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);
    arguments.setFileName(request.getParameter("FileName"));
    try {
      arguments.setIfModifiedSince(request.getDateHeader("If-Modified-Since"));
    } catch (IllegalArgumentException e) {
      arguments.setIfModifiedSince(0);
    }
    arguments.setIfNoneMatch(request.getHeader("If-None-Match"));
  }

  /**
   * Validates thumbnail file properties and current user access rights.
   *
   * @throws ConnectorException when validation fails.
   */
  private void validate(ThumbnailArguments arguments, IConfiguration configuration) throws ConnectorException {
    if (!configuration.isThumbsEnabled()) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_THUMBNAILS_DISABLED);
    }

    if (arguments.getType() == null) {
      throw new ConnectorException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameInvalid(arguments.getFileName())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (FileUtils.isFileHidden(arguments.getFileName(), configuration)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
    }

    log.debug("configuration thumbsPath: {}", configuration.getThumbsPath());
    Path fullCurrentDir = Paths.get(configuration.getThumbsPath(), arguments.getType().getName(), arguments.getCurrentFolder());
    log.debug("typeThumbDir: {}", fullCurrentDir);

    try {
      String fullCurrentPath = fullCurrentDir.toAbsolutePath().toString();
      log.debug(fullCurrentPath);
      arguments.setFullCurrentPath(fullCurrentPath);
      if (!Files.exists(fullCurrentDir)) {
        Files.createDirectories(fullCurrentDir);
      }
    } catch (IOException | SecurityException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }

  }

  /**
   * Creates thumbnail file if thumbnails are enabled and thumb file doesn't
   * exists.
   *
   * @throws ConnectorException when thumbnail creation fails.
   */
  @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
  private void createThumb(ThumbnailArguments arguments, IConfiguration configuration) throws ConnectorException {
    log.debug("ThumbnailCommand.createThumb()");
    log.debug("{}", arguments.getFullCurrentPath());
    Path thumbFile = Paths.get(arguments.getFullCurrentPath(), arguments.getFileName());
    log.debug("thumbFile: {}", thumbFile);
    arguments.setThumbFile(thumbFile);
    try {
      if (!Files.exists(thumbFile)) {
        Path orginFile = Paths.get(arguments.getType().getPath(),
                arguments.getCurrentFolder(), arguments.getFileName());
        log.debug("orginFile: {}", orginFile);
        if (!Files.exists(orginFile)) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
        }
        try {
          ImageUtils.createThumb(orginFile, thumbFile, configuration);
        } catch (Exception e) {
          try {
            Files.deleteIfExists(thumbFile);
          } catch (IOException ex) {
            e.addSuppressed(ex);
          }
          throw new ConnectorException(
                  Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                  e);
        }
      }
    } catch (SecurityException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }

  }

  /**
   * Fills in response headers after creating file.
   *
   * @return true if continue returning thumb or false if break and send
   * response code.
   * @throws ConnectorException when access is denied.
   */
  private boolean setResponseHeadersAfterCreatingFile(HttpServletResponse response,
          ThumbnailArguments arguments) throws ConnectorException {
    // Set content size
    Path file = Paths.get(arguments.getFullCurrentPath(), arguments.getFileName());
    try {
      FileTime lastModifiedTime = Files.getLastModifiedTime(file);
      String etag = "W/\"" + Long.toHexString(lastModifiedTime.toMillis()) + "-" + Long.toHexString(Files.size(file)) + '"';
      Instant instant = lastModifiedTime.toInstant();
      response.setHeader("Etag", etag);
      response.setHeader("Last-Modified", FORMATTER.format(instant));

      if (etag.equals(arguments.getIfNoneMatch())
              || lastModifiedTime.toMillis() <= arguments.getIfModifiedSince() + 1000L) {
        return false;
      }

      response.setContentLengthLong(Files.size(file));

      return true;
    } catch (IOException | SecurityException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }
  }

}
