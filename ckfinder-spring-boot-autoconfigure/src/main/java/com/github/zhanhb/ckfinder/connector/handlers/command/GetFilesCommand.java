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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.GetFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.File;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
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
  private static final float BYTES = 1024f;

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
  protected void createXMLChildNodes(Connector.Builder rootElement, GetFilesParameter param, IConfiguration configuration) {
    createFilesData(rootElement, param, configuration);
  }

  /**
   * gets data to XML response.
   *
   * @param param
   * @param configuration
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected void createXml(GetFilesParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    param.setFullCurrentPath(Paths.get(param.getType().getPath(),
            param.getCurrentFolder()).toString());

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    Path dir = Paths.get(param.getFullCurrentPath());
    try {
      if (!Files.exists(dir)) {
        param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND);
      }
      param.setFiles(FileUtils.findChildrensList(dir, false));
    } catch (IOException | SecurityException e) {
      log.error("", e);
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
    filterListByHiddenAndNotAllowed(param, configuration);
    Collections.sort(param.getFiles());
  }

  /**
   *
   *
   */
  private void filterListByHiddenAndNotAllowed(GetFilesParameter param, IConfiguration configuration) {
    List<String> tmpFiles = param.getFiles().stream()
            .filter(file -> (FileUtils.isFileExtensionAllwed(file, param.getType())
            && !FileUtils.isFileHidden(file, configuration)))
            .collect(Collectors.toList());

    param.getFiles().clear();
    param.getFiles().addAll(tmpFiles);

  }

  /**
   * creates files data node in response XML.
   *
   * @param rootElement root element from XML.
   */
  private void createFilesData(Connector.Builder rootElement, GetFilesParameter param, IConfiguration configuration) {
    com.github.zhanhb.ckfinder.connector.handlers.response.Files.Builder files = com.github.zhanhb.ckfinder.connector.handlers.response.Files.builder();
    for (String filePath : param.getFiles()) {
      Path file = Paths.get(param.getFullCurrentPath(), filePath);
      if (Files.exists(file)) {
        try {
          File.Builder builder = File.builder()
                  .name(filePath)
                  .date(FileUtils.parseLastModifDate(file))
                  .size(getSize(file));
          if (ImageUtils.isImageExtension(file) && isAddThumbsAttr(param, configuration)) {
            String attr = createThumbAttr(file, param, configuration);
            if (!attr.isEmpty()) {
              builder.thumb(attr);
            }
          }
          files.file(builder.build());
        } catch (IOException ex) {
        }
      }
    }
    rootElement.files(files.build());
  }

  /**
   * gets thumb attribute value.
   *
   * @param file file to check if has thumb.
   * @return thumb attribute values
   */
  private String createThumbAttr(Path file, GetFilesParameter param, IConfiguration configuration) {
    Path thumbFile = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder(),
            file.getFileName().toString());
    if (Files.exists(thumbFile)) {
      return file.getFileName().toString();
    } else if (isShowThumbs(param)) {
      return "?".concat(file.getFileName().toString());
    } else {
      return "";
    }
  }

  /**
   * get file size.
   *
   * @param file file
   * @return file size
   */
  private String getSize(Path file) throws IOException {
    long size = Files.size(file);
    if (size > 0 && size < BYTES) {
      return "1";
    } else {
      return String.valueOf(Math.round(size / BYTES));
    }
  }

  /**
   * Check if show thumbs or not (add attr to file node with thumb file name).
   *
   * @return true if show thumbs
   */
  private boolean isAddThumbsAttr(GetFilesParameter param, IConfiguration configuration) {
    return configuration.isThumbsEnabled()
            && (configuration.isThumbsDirectAccess()
            || isShowThumbs(param));
  }

  /**
   * checks show thumb request attribute.
   *
   * @return true if is set.
   */
  private boolean isShowThumbs(GetFilesParameter param) {
    return "1".equals(param.getShowThumbs());
  }

}
