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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.XMLArguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>DeleteFolder</code> command.
 */
@Slf4j
public class DeleteFolderCommand extends OnSuccessXmlCommand<XMLArguments> implements IPostCommand {

  public DeleteFolderCommand() {
    super(XMLArguments::new);
  }

  @Override
  protected void createXMLChildNodesInternal(Connector.Builder rootElement, XMLArguments arguments, IConfiguration configuration) {
  }

  /**
   * @param arguments
   * @param configuration connector configuration
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected void createXml(XMLArguments arguments, IConfiguration configuration) throws ConnectorException {
    if (arguments.getType() == null) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(),
            arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_DELETE)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }
    if (arguments.getCurrentFolder().equals("/")) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (FileUtils.isDirectoryHidden(arguments.getCurrentFolder(), configuration)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path dir = Paths.get(arguments.getType().getPath(), arguments.getCurrentFolder());

    try {
      if (!Files.isDirectory(dir)) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND);
      }

      if (FileUtils.delete(dir)) {
        Path thumbDir = Paths.get(configuration.getThumbsPath(),
                arguments.getType().getName(), arguments.getCurrentFolder());
        FileUtils.delete(thumbDir);
      } else {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
      }
    } catch (SecurityException e) {
      log.error("", e);
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }

  }

}
