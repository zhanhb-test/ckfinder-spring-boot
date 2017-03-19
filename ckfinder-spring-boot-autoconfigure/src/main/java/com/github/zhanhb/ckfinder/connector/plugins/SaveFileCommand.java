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
package com.github.zhanhb.ckfinder.connector.plugins;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.SaveFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveFileCommand extends BaseXmlCommand<SaveFileParameter> implements IPostCommand {

  public SaveFileCommand() {
    super(SaveFileParameter::new);
  }

  @Override
  protected void createXml(Connector.Builder rootElement, SaveFileParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_DELETE)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (param.getFileName() == null || param.getFileName().isEmpty()) {
      param.throwException(ConnectorError.INVALID_NAME);
    }

    if (param.getFileContent() == null || param.getFileContent().isEmpty()) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(), param.getType())) {
      param.throwException(ConnectorError.INVALID_EXTENSION);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    Path sourceFile = Paths.get(param.getType().getPath(),
            param.getCurrentFolder(), param.getFileName());

    try {
      if (!Files.isRegularFile(sourceFile)) {
        param.throwException(ConnectorError.FILE_NOT_FOUND);
      }
      Files.write(sourceFile, param.getFileContent().getBytes(StandardCharsets.UTF_8));
    } catch (SecurityException | IOException e) {
      log.error("", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  @Override
  protected void initParams(SaveFileParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setCurrentFolder(request.getParameter("currentFolder"));
    param.setType(configuration.getTypes().get(request.getParameter("type")));
    param.setFileContent(request.getParameter("content"));
    param.setFileName(request.getParameter("fileName"));
    param.setRequest(request);
  }

}
