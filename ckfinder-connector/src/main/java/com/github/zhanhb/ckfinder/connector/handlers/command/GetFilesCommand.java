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

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ThumbnailProperties;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.FileElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.FilesElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>GetFiles</code> command. Get files from current folder
 * command.
 */
@Slf4j
public class GetFilesCommand extends FinishOnErrorXmlCommand<String> {

  /**
   * number of bytes in kilobyte.
   */
  private static final BigDecimal BYTES = BigDecimal.valueOf(1024);

  /**
   * initializing parameters for command handler.
   *
   * @param request request
   * @param context ckfinder context
   * @return the parameter
   */
  @Override
  protected String parseParameters(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("showThumbs");
  }

  @Override
  protected void createXml(@Nullable String showThumbs, CommandContext cmdContext,
          ConnectorElement.Builder rootElement) throws ConnectorException {
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_VIEW);

    Path dir = cmdContext.checkDirectory();

    try {
      List<Path> files = FileUtils.listChildren(dir, false);
      createFilesData(files, rootElement, showThumbs, cmdContext);
    } catch (IOException e) {
      log.error("", e);
      throw cmdContext.toException(ErrorCode.ACCESS_DENIED).initCause(e);
    }
  }

  /**
   * creates files data node in response XML.
   *
   * @param list
   * @param rootElement root element from XML.
   * @param showThumbs the request parameter showThumbs
   * @param cmdContext command context
   */
  private void createFilesData(List<? extends Path> list, ConnectorElement.Builder rootElement,
          String showThumbs, CommandContext cmdContext) {
    CKFinderContext context = cmdContext.getCfCtx();
    FilesElement.Builder files = FilesElement.builder();
    for (Path file : list) {
      String fileName = file.getFileName().toString();
      if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
        continue;
      }
      if (context.isFileHidden(fileName)) {
        continue;
      }
      BasicFileAttributes attrs;
      try {
        attrs = Files.readAttributes(file, BasicFileAttributes.class);
      } catch (IOException ex) {
        continue;
      }
      files.file(FileElement.builder()
              .name(file.getFileName().toString())
              .date(FileUtils.parseLastModifiedDate(attrs))
              .size(getSizeInKB(attrs).longValue())
              .thumb(createThumbAttr(file, showThumbs, cmdContext))
              .build());
    }
    rootElement.result(files.build());
  }

  /**
   * gets thumb attribute value.
   *
   * @param file file to check if has thumb.
   * @param showThumbs request parameter showThumbs
   * @param cmdContext command context
   * @return thumb attribute values
   */
  private String createThumbAttr(Path file, String showThumbs, CommandContext cmdContext) {
    if (ImageUtils.isImageExtension(file) && isAddThumbsAttr(showThumbs, cmdContext.getCfCtx().getThumbnail())) {
      return cmdContext.resolveThumbnail(file.getFileName().toString()).map(thumbFile -> {
        if (Files.exists(thumbFile)) {
          return file.getFileName().toString();
        } else if (requestShowThumbs(showThumbs)) {
          return "?".concat(file.getFileName().toString());
        }
        return null;
      }).orElse(null);
    }
    return null;
  }

  /**
   * get file size.
   *
   * @param attributes file attributes
   * @return file size
   */
  private BigDecimal getSizeInKB(BasicFileAttributes attributes) {
    long size = attributes.size();
    return size > 0 && size <= 1024 ? BigDecimal.ONE
            : BigDecimal.valueOf(size).divide(BYTES, 0, RoundingMode.HALF_EVEN);
  }

  /**
   * Check if show thumbs or not (add attr to file node with thumb file name).
   *
   * @param showThumbs request parameter showThumbs
   * @param thumbnail
   * @return true if show thumbs
   */
  private boolean isAddThumbsAttr(String showThumbs, ThumbnailProperties thumbnail) {
    return thumbnail != null && (thumbnail.isDirectAccess() || requestShowThumbs(showThumbs));
  }

  /**
   * checks show thumb request attribute.
   *
   * @param showThumbs request parameter showThumbs
   * @return true if is set.
   */
  private boolean requestShowThumbs(String showThumbs) {
    return "1".equals(showThumbs);
  }

}
