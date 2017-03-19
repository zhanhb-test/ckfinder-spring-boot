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

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ThumbnailParameter;
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
public class ThumbnailCommand extends Command<ThumbnailParameter> {

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
          .withZone(ZoneId.of("GMT"));

  public ThumbnailCommand() {
    super(ThumbnailParameter::new);
  }

  /**
   * Gets mime type of image.
   *
   * @param sc the {@code ServletContext} object.
   * @param response currect response object
   * @param param
   * @return mime type of the image.
   */
  private String getMimeTypeOfImage(ServletContext sc,
          HttpServletResponse response, ThumbnailParameter param) {
    String fileName = param.getFileName();
    if (fileName != null && fileName.length() != 0) {
      String fileExtension = FileUtils.getFileExtension(fileName);
      if (fileExtension != null) {
        String tempFileName = fileName.substring(0, fileName.lastIndexOf('.') + 1).concat(fileExtension.toLowerCase());
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
  final void execute(ThumbnailParameter param, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration) throws ConnectorException, IOException {
    validate(param, configuration);
    createThumb(param, configuration);
    response.setHeader("Cache-Control", "public");
    String mimetype = getMimeTypeOfImage(request.getServletContext(), response, param);
    if (mimetype != null) {
      response.setContentType(mimetype);
    }
    response.addHeader("Content-Disposition",
            ContentDisposition.getContentDisposition("attachment", param.getFileName()));
    if (setResponseHeadersAfterCreatingFile(response, param)) {
      try (ServletOutputStream out = response.getOutputStream()) {
        Files.copy(param.getThumbFile(), out);
      } catch (IOException e) {
        log.error("", e);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
      }
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }
  }

  @Override
  protected void initParams(ThumbnailParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setFileName(request.getParameter("FileName"));
    try {
      param.setIfModifiedSince(request.getDateHeader("If-Modified-Since"));
    } catch (IllegalArgumentException e) {
      param.setIfModifiedSince(0);
    }
    param.setIfNoneMatch(request.getHeader("If-None-Match"));
  }

  /**
   * Validates thumbnail file properties and current user access rights.
   *
   * @param param
   * @param configuration
   * @throws ConnectorException when validation fails.
   */
  private void validate(ThumbnailParameter param, IConfiguration configuration) throws ConnectorException {
    if (!configuration.isThumbsEnabled()) {
      param.throwException(ConnectorError.THUMBNAILS_DISABLED);
    }

    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_VIEW)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (FileUtils.isFileHidden(param.getFileName(), configuration)) {
      param.throwException(ConnectorError.FILE_NOT_FOUND);
    }

    log.debug("configuration thumbsPath: {}", configuration.getThumbsPath());
    Path fullCurrentDir = Paths.get(configuration.getThumbsPath(), param.getType().getName(), param.getCurrentFolder());
    log.debug("typeThumbDir: {}", fullCurrentDir);

    try {
      String fullCurrentPath = fullCurrentDir.toString();
      log.debug(fullCurrentPath);
      param.setFullCurrentPath(fullCurrentPath);
      if (!Files.exists(fullCurrentDir)) {
        Files.createDirectories(fullCurrentDir);
      }
    } catch (IOException | SecurityException e) {
      throw new ConnectorException(ConnectorError.ACCESS_DENIED, e);
    }

  }

  /**
   * Creates thumbnail file if thumbnails are enabled and thumb file doesn't
   * exists.
   *
   * @param param
   * @param configuration
   * @throws ConnectorException when thumbnail creation fails.
   */
  @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
  private void createThumb(ThumbnailParameter param, IConfiguration configuration) throws ConnectorException {
    log.debug("ThumbnailCommand.createThumb()");
    log.debug("{}", param.getFullCurrentPath());
    Path thumbFile = Paths.get(param.getFullCurrentPath(), param.getFileName());
    log.debug("thumbFile: {}", thumbFile);
    param.setThumbFile(thumbFile);
    try {
      if (!Files.exists(thumbFile)) {
        Path orginFile = Paths.get(param.getType().getPath(),
                param.getCurrentFolder(), param.getFileName());
        log.debug("orginFile: {}", orginFile);
        if (!Files.exists(orginFile)) {
          param.throwException(ConnectorError.FILE_NOT_FOUND);
        }
        try {
          boolean success = ImageUtils.createThumb(orginFile, thumbFile, configuration);
          if (!success) {
            param.throwException(ConnectorError.FILE_NOT_FOUND);
          }
        } catch (IOException e) {
          try {
            Files.deleteIfExists(thumbFile);
          } catch (IOException ex) {
            e.addSuppressed(ex);
          }
          throw new ConnectorException(ConnectorError.ACCESS_DENIED, e);
        }
      }
    } catch (SecurityException e) {
      throw new ConnectorException(ConnectorError.ACCESS_DENIED, e);
    }

  }

  /**
   * Fills in response headers after creating file.
   *
   * @param response
   * @param param
   * @return true if continue returning thumb or false if break and send
   * response code.
   * @throws ConnectorException when access is denied.
   */
  private boolean setResponseHeadersAfterCreatingFile(HttpServletResponse response,
          ThumbnailParameter param) throws ConnectorException {
    // Set content size
    Path file = Paths.get(param.getFullCurrentPath(), param.getFileName());
    try {
      FileTime lastModifiedTime = Files.getLastModifiedTime(file);
      String etag = "W/\"" + Long.toHexString(lastModifiedTime.toMillis()) + "-" + Long.toHexString(Files.size(file)) + '"';
      Instant instant = lastModifiedTime.toInstant();
      response.setHeader("Etag", etag);
      response.setHeader("Last-Modified", FORMATTER.format(instant));

      if (etag.equals(param.getIfNoneMatch())
              || lastModifiedTime.toMillis() <= param.getIfModifiedSince() + 1000L) {
        return false;
      }

      response.setContentLengthLong(Files.size(file));

      return true;
    } catch (IOException | SecurityException e) {
      throw new ConnectorException(ConnectorError.ACCESS_DENIED, e);
    }
  }

}
