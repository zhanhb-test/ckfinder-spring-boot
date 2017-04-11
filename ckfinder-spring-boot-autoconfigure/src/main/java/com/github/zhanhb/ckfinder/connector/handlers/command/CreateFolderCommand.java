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
import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CreateFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.NewFolder;
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
   * @throws ConnectorException when error occurs
   */
  @Override
  protected void createXml(Connector.Builder rootElement, CreateFolderParameter param, Configuration configuration) throws ConnectorException {
    checkRequestPathValid(param.getNewFolderName());

    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FOLDER_CREATE)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToAscii(param.getNewFolderName()));
    }

    if (!FileUtils.isFolderNameValid(param.getNewFolderName(), configuration)) {
      param.throwException(ErrorCode.INVALID_NAME);
    }
    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }
    if (configuration.isDirectoryHidden(param.getNewFolderName())) {
      param.throwException(ErrorCode.INVALID_NAME);
    }

    Path dir = getPath(param.getType().getPath(),
            param.getCurrentFolder(), param.getNewFolderName());
    if (Files.exists(dir)) {
      param.throwException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    rootElement.result(NewFolder.builder()
            .name(param.getNewFolderName())
            .build());
  }

  @Override
  protected CreateFolderParameter popupParams(HttpServletRequest request, Configuration configuration)
          throws ConnectorException {
    CreateFolderParameter param = doInitParam(new CreateFolderParameter(), request, configuration);
    param.setNewFolderName(request.getParameter("NewFolderName"));
    return param;
  }

}
