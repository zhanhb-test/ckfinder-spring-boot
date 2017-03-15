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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CreateFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.NewFolder;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CreateFolder</code> command. Create subfolder.
 */
@Slf4j
public class CreateFolderCommand extends BaseXmlCommand<CreateFolderParameter> implements IPostCommand {

  public CreateFolderCommand() {
    super(CreateFolderParameter::new);
  }

  /**
   * creates current folder XML node.
   *
   * @param rootElement XML root element.
   */
  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, CreateFolderParameter param, IConfiguration configuration) {
    rootElement.newFolder(NewFolder.builder()
            .name(param.getNewFolderName())
            .build());
  }

  /**
   * gets data for xml. Not used in this handler.
   *
   * @param param
   * @param configuration connector configuration
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected void createXml(CreateFolderParameter param, IConfiguration configuration)
          throws ConnectorException {
    checkRequestPathValid(param.getNewFolderName());

    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_CREATE)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToASCII(param.getNewFolderName()));
    }

    if (!FileUtils.isFolderNameInvalid(param.getNewFolderName(), configuration)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
    }
    if (FileUtils.isDirectoryHidden(param.getCurrentFolder(), configuration)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }
    if (FileUtils.isDirectoryHidden(param.getNewFolderName(), configuration)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
    }

    try {
      if (createFolder(param)) {
      } else {
        param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
      }

    } catch (SecurityException e) {
      log.error("", e);
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
  }

  /**
   * creates folder. throws Exception when security problem occurs or folder
   * already exists
   *
   * @return true if folder is created correctly
   * @throws ConnectorException when error occurs or dir exists
   */
  private boolean createFolder(CreateFolderParameter param) throws ConnectorException {
    Path dir = Paths.get(param.getType().getPath(),
            param.getCurrentFolder(), param.getNewFolderName());
    if (Files.exists(dir)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST);
    }
    try {
      Files.createDirectories(dir);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  @Override
  protected void initParams(CreateFolderParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setNewFolderName(request.getParameter("NewFolderName"));
  }

}
