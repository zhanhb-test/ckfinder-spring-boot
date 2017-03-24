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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.GetFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.File;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

  public GetFilesCommand() {
    super(GetFilesParameter::new);
  }

  /**
   * initializing parameters for command handler.
   *
   * @param param
   * @param request request
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs
   */
  @Override
  protected void initParams(GetFilesParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);

    param.setShowThumbs(request.getParameter("showThumbs"));
  }

  @Override
  protected void createXml(Connector.Builder rootElement, GetFilesParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_VIEW)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    Path dir = Paths.get(param.getType().getPath(), param.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      param.throwException(ConnectorError.FOLDER_NOT_FOUND);
    }

    try {
      List<Path> files = FileUtils.listChildren(dir, false);
      createFilesData(files, rootElement, param, configuration);
    } catch (IOException e) {
      log.error("", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  /**
   * creates files data node in response XML.
   *
   * @param list
   * @param rootElement root element from XML.
   * @param param
   * @param configuration
   */
  private void createFilesData(List<Path> list, Connector.Builder rootElement, GetFilesParameter param, IConfiguration configuration) {
    com.github.zhanhb.ckfinder.connector.handlers.response.Files.Builder files = com.github.zhanhb.ckfinder.connector.handlers.response.Files.builder();
    for (Path file : list) {
      String fileName = file.getFileName().toString();
      if (!FileUtils.isFileExtensionAllowed(fileName, param.getType())) {
        continue;
      }
      if (configuration.isFileHidden(fileName)) {
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
              .date(FileUtils.parseLastModifDate(attrs))
              .size(getSizeInKB(attrs).longValue())
              .thumb(createThumbAttr(file, param, configuration))
              .build());
    }
    rootElement.result(files.build());
  }

  /**
   * gets thumb attribute value.
   *
   * @param file file to check if has thumb.
   * @param param
   * @param configuration
   * @return thumb attribute values
   */
  private String createThumbAttr(Path file, GetFilesParameter param, IConfiguration configuration) {
    if (ImageUtils.isImageExtension(file) && isAddThumbsAttr(param, configuration)) {
      Path thumbFile = Paths.get(configuration.getThumbsPath(),
              param.getType().getName(), param.getCurrentFolder(),
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
    if (size > 0) {
      return size > 0 && size <= 1024 ? BigDecimal.ONE
              : BigDecimal.valueOf(size).divide(BYTES, 0, RoundingMode.HALF_EVEN);
    }
    return BigDecimal.ZERO;
  }

  /**
   * Check if show thumbs or not (add attr to file node with thumb file name).
   *
   * @param param
   * @param configuration
   * @return true if show thumbs
   */
  private boolean isAddThumbsAttr(GetFilesParameter param, IConfiguration configuration) {
    return configuration.isThumbsEnabled()
            && (configuration.isThumbsDirectAccess()
            || requestShowThumbs(param));
  }

  /**
   * checks show thumb request attribute.
   *
   * @param param
   * @return true if is set.
   */
  private boolean requestShowThumbs(GetFilesParameter param) {
    return "1".equals(param.getShowThumbs());
  }

}
