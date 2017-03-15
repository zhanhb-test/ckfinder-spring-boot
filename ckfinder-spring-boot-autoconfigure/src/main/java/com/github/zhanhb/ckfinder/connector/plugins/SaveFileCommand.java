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

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.SaveFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveFileCommand extends BaseXmlCommand<SaveFileParameter> {

  public SaveFileCommand() {
    super(SaveFileParameter::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, SaveFileParameter param, IConfiguration configuration) {
  }

  @Override
  protected void createXml(SaveFileParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (param.getFileName() == null || param.getFileName().isEmpty()) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
    }

    if (param.getFileContent() == null || param.getFileContent().isEmpty()) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllwed(param.getFileName(), param.getType())) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION);
    }

    if (!FileUtils.isFileNameInvalid(param.getFileName())) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path sourceFile = Paths.get(param.getType().getPath(),
            param.getCurrentFolder(), param.getFileName());

    try {
      if (!Files.isRegularFile(sourceFile)) {
        param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }
      Files.write(sourceFile, param.getFileContent().getBytes("UTF-8"));
    } catch (FileNotFoundException e) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
    } catch (SecurityException | IOException e) {
      log.error("", e);
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
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
