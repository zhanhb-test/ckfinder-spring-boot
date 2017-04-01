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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CreateFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.NewFolder;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CreateFolder</code> command. Create subfolder.
 */
@Slf4j
public class CreateFolderCommand extends BaseXmlCommand<CreateFolderParameter> implements IPostCommand {

  /**
   * creates current folder XML node.
   *
   * @param rootElement XML root element.
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected void createXml(Connector.Builder rootElement, CreateFolderParameter param, IConfiguration configuration) throws ConnectorException {
    checkRequestPathValid(param.getNewFolderName());

    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FOLDER_CREATE)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToAscii(param.getNewFolderName()));
    }

    if (!FileUtils.isFolderNameValid(param.getNewFolderName(), configuration)) {
      param.throwException(ConnectorError.INVALID_NAME);
    }
    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }
    if (configuration.isDirectoryHidden(param.getNewFolderName())) {
      param.throwException(ConnectorError.INVALID_NAME);
    }

    Path dir = getPath(param.getType().getPath(),
            param.getCurrentFolder(), param.getNewFolderName());
    if (Files.exists(dir)) {
      param.throwException(ConnectorError.ALREADY_EXIST);
    }
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    rootElement.result(NewFolder.builder()
            .name(param.getNewFolderName())
            .build());
  }

  @Override
  protected CreateFolderParameter popupParams(HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    CreateFolderParameter param = doInitParam(new CreateFolderParameter(), request, configuration);
    param.setNewFolderName(request.getParameter("NewFolderName"));
    return param;
  }

}
