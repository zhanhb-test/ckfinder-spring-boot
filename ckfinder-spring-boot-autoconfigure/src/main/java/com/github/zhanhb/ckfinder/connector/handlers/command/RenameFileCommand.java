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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFile;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>RenameFile</code> command.
 */
@Slf4j
public class RenameFileCommand extends ErrorListXMLCommand<RenameFileParameter> implements IPostCommand {

  public RenameFileCommand() {
    super(RenameFileParameter::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, RenameFileParameter param, IConfiguration configuration) {
    if (param.isAddRenameNode()) {
      createRenamedFileNode(rootElement, param);
    }
  }

  /**
   * create rename file XML node.
   *
   * @param rootElement XML root node
   */
  private void createRenamedFileNode(Connector.Builder rootElement, RenameFileParameter param) {
    RenamedFile.Builder element = RenamedFile.builder().name(param.getFileName());
    if (param.isRenamed()) {
      element.newName(param.getNewFileName());
    }
    rootElement.renamedFile(element.build());
  }

  /**
   * gets data for XML and checks all validation.
   *
   * @param param
   * @param configuration connector configuration
   * @return error code or 0 if it's correct.
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected int getDataForXml(RenameFileParameter param, IConfiguration configuration)
          throws ConnectorException {
    log.trace("getDataForXml");
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_RENAME)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToASCII(param.getNewFileName()));
    }

    if (param.getFileName() != null && !param.getFileName().isEmpty()
            && param.getNewFileName() != null && !param.getNewFileName().isEmpty()) {
      param.setAddRenameNode(true);
    }

    if (!FileUtils.isFileExtensionAllwed(param.getNewFileName(),
            param.getType())) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION;
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(param.getType(),
              param.getNewFileName()));
    }

    if (!FileUtils.isFileNameInvalid(param.getFileName())
            || FileUtils.isFileHidden(param.getFileName(), configuration)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
    }

    if (!FileUtils.isFileNameInvalid(param.getNewFileName(), configuration)
            || FileUtils.isFileHidden(param.getNewFileName(), configuration)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME;
    }

    if (!FileUtils.isFileExtensionAllwed(param.getFileName(),
            param.getType())) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
    }

    String dirPath = param.getType().getPath();
    Path file = Paths.get(dirPath, param.getCurrentFolder(), param.getFileName());
    Path newFile = Paths.get(dirPath, param.getCurrentFolder(), param.getNewFileName());

    try {
      if (!Files.exists(file)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND;
      }

      if (Files.exists(newFile)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST;
      }

      try {
        Files.move(file, newFile);
        param.setRenamed(true);
        renameThumb(param, configuration);
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE;
      } catch (IOException ex) {
        param.setRenamed(false);
        log.error("IOException", ex);
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
      }
    } catch (SecurityException e) {
      log.error("", e);
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED;
    }

  }

  /**
   * rename thumb file.
   */
  private void renameThumb(RenameFileParameter param, IConfiguration configuration) throws IOException {
    Path thumbFile = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder(),
            param.getFileName());
    Path newThumbFile = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder(),
            param.getNewFileName());

    try {
      Files.move(thumbFile, newThumbFile);
    } catch (IOException ex) {
    }
  }

  @Override
  protected void initParams(RenameFileParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
  }

}
