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

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.SaveFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveFileCommand extends BaseXmlCommand<SaveFileParameter> implements IPostCommand {

  @Override
  protected void createXml(Connector.Builder rootElement, SaveFileParameter param, Configuration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_DELETE)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (param.getFileName() == null || param.getFileName().isEmpty()) {
      param.throwException(ErrorCode.INVALID_NAME);
    }

    if (param.getFileContent() == null || param.getFileContent().isEmpty()) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(), param.getType())) {
      param.throwException(ErrorCode.INVALID_EXTENSION);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path sourceFile = getPath(param.getType().getPath(),
            param.getCurrentFolder(), param.getFileName());

    if (!Files.isRegularFile(sourceFile)) {
      param.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    try {
      Files.write(sourceFile, param.getFileContent().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.error("", e);
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  @Override
  protected SaveFileParameter popupParams(HttpServletRequest request, Configuration configuration)
          throws ConnectorException {
    SaveFileParameter param = doInitParam(new SaveFileParameter(), request, configuration);
    param.setFileContent(request.getParameter("content"));
    param.setFileName(request.getParameter("fileName"));
    return param;
  }

}
