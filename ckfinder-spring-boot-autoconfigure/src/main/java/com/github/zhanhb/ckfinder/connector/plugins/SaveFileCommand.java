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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.SaveFileArguments;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
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
public class SaveFileCommand extends BaseXmlCommand<SaveFileArguments> {

  public SaveFileCommand() {
    super(SaveFileArguments::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, SaveFileArguments arguments, IConfiguration configuration) {
  }

  @Override
  protected void createXml(SaveFileArguments arguments, IConfiguration configuration) throws ConnectorException {
    if (arguments.getType() == null) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (arguments.getFileName() == null || arguments.getFileName().isEmpty()) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
    }

    if (arguments.getFileContent() == null || arguments.getFileContent().isEmpty()) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllwed(arguments.getFileName(), arguments.getType())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION);
    }

    if (!FileUtils.isFileNameInvalid(arguments.getFileName())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path sourceFile = Paths.get(arguments.getType().getPath(),
            arguments.getCurrentFolder(), arguments.getFileName());

    try {
      if (!Files.isRegularFile(sourceFile)) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }
      Files.write(sourceFile, arguments.getFileContent().getBytes("UTF-8"));
    } catch (FileNotFoundException e) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
    } catch (SecurityException | IOException e) {
      log.error("", e);
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
  }

  @Override
  protected void initParams(SaveFileArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);
    arguments.setCurrentFolder(request.getParameter("currentFolder"));
    arguments.setType(configuration.getTypes().get(request.getParameter("type")));
    arguments.setFileContent(request.getParameter("content"));
    arguments.setFileName(request.getParameter("fileName"));
    arguments.setRequest(request);
  }

}
