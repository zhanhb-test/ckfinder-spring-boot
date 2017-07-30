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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.GetFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.File;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>GetFiles</code> command. Get files from current folder
 * command.
 */
@Slf4j
public class GetFilesCommand extends BaseXmlCommand<GetFilesParameter> {

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
   * @throws ConnectorException when error occurs
   */
  @Override
  protected GetFilesParameter popupParams(HttpServletRequest request, CKFinderContext context)
          throws ConnectorException {
    GetFilesParameter param = doInitParam(new GetFilesParameter(), request, context);
    param.setShowThumbs(request.getParameter("showThumbs"));
    return param;
  }

  @Override
  protected void createXml(Connector.Builder rootElement, GetFilesParameter param, CKFinderContext context) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!context.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_VIEW)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    Path dir = getPath(param.getType().getPath(), param.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      param.throwException(ErrorCode.FOLDER_NOT_FOUND);
    }

    try {
      List<Path> files = FileUtils.listChildren(dir, false);
      createFilesData(files, rootElement, param, context);
    } catch (IOException e) {
      log.error("", e);
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * creates files data node in response XML.
   *
   * @param list
   * @param rootElement root element from XML.
   * @param param the parameter
   * @param context ckfinder context
   */
  private void createFilesData(List<Path> list, Connector.Builder rootElement, GetFilesParameter param, CKFinderContext context) {
    com.github.zhanhb.ckfinder.connector.handlers.response.Files.Builder files = com.github.zhanhb.ckfinder.connector.handlers.response.Files.builder();
    for (Path file : list) {
      String fileName = file.getFileName().toString();
      if (!FileUtils.isFileExtensionAllowed(fileName, param.getType())) {
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
      files.file(File.builder()
              .name(file.getFileName().toString())
              .date(FileUtils.parseLastModifiedDate(attrs))
              .size(getSizeInKB(attrs).longValue())
              .thumb(createThumbAttr(file, param, context))
              .build());
    }
    rootElement.result(files.build());
  }

  /**
   * gets thumb attribute value.
   *
   * @param file file to check if has thumb.
   * @param param the parameter
   * @param context ckfinder context
   * @return thumb attribute values
   */
  private String createThumbAttr(Path file, GetFilesParameter param, CKFinderContext context) {
    if (ImageUtils.isImageExtension(file) && isAddThumbsAttr(param, context.getThumbnail())) {
      Path thumbFile = getPath(param.getType().getThumbnailPath(), param.getCurrentFolder(),
              file.getFileName().toString());
      if (Files.exists(thumbFile)) {
        return file.getFileName().toString();
      } else if (requestShowThumbs(param)) {
        return "?".concat(file.getFileName().toString());
      }
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
   * @param param the parameter
   * @param thumbnail
   * @return true if show thumbs
   */
  private boolean isAddThumbsAttr(GetFilesParameter param, ThumbnailProperties thumbnail) {
    return thumbnail != null && (thumbnail.isDirectAccess() || requestShowThumbs(param));
  }

  /**
   * checks show thumb request attribute.
   *
   * @param param the parameter
   * @return true if is set.
   */
  private boolean requestShowThumbs(GetFilesParameter param) {
    return "1".equals(param.getShowThumbs());
  }

}
