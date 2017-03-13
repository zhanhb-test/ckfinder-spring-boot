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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.RenameFileArguments;
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
public class RenameFileCommand extends ErrorListXMLCommand<RenameFileArguments> implements IPostCommand {

  public RenameFileCommand() {
    super(RenameFileArguments::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, RenameFileArguments arguments, IConfiguration configuration) {
    if (arguments.isAddRenameNode()) {
      createRenamedFileNode(rootElement, arguments);
    }
  }

  /**
   * create rename file XML node.
   *
   * @param rootElement XML root node
   */
  private void createRenamedFileNode(Connector.Builder rootElement, RenameFileArguments arguments) {
    RenamedFile.Builder element = RenamedFile.builder().name(arguments.getFileName());
    if (arguments.isRenamed()) {
      element.newName(arguments.getNewFileName());
    }
    rootElement.renamedFile(element.build());
  }

  /**
   * gets data for XML and checks all validation.
   *
   * @param arguments
   * @param configuration connector configuration
   * @return error code or 0 if it's correct.
   */
  @Override
  protected int getDataForXml(RenameFileArguments arguments, IConfiguration configuration) {
    log.trace("getDataForXml");
    if (arguments.getType() == null) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE;
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_RENAME)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED;
    }

    if (configuration.isForceAscii()) {
      arguments.setNewFileName(FileUtils.convertToASCII(arguments.getNewFileName()));
    }

    if (arguments.getFileName() != null && !arguments.getFileName().isEmpty()
            && arguments.getNewFileName() != null && !arguments.getNewFileName().isEmpty()) {
      arguments.setAddRenameNode(true);
    }

    if (!FileUtils.isFileExtensionAllwed(arguments.getNewFileName(),
            arguments.getType())) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION;
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      arguments.setNewFileName(FileUtils.renameFileWithBadExt(arguments.getType(),
              arguments.getNewFileName()));
    }

    if (!FileUtils.isFileNameInvalid(arguments.getFileName())
            || FileUtils.isFileHidden(arguments.getFileName(), configuration)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
    }

    if (!FileUtils.isFileNameInvalid(arguments.getNewFileName(), configuration)
            || FileUtils.isFileHidden(arguments.getNewFileName(), configuration)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME;
    }

    if (!FileUtils.isFileExtensionAllwed(arguments.getFileName(),
            arguments.getType())) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
    }

    String dirPath = arguments.getType().getPath();
    Path file = Paths.get(dirPath, arguments.getCurrentFolder(), arguments.getFileName());
    Path newFile = Paths.get(dirPath, arguments.getCurrentFolder(), arguments.getNewFileName());

    try {
      if (!Files.exists(file)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND;
      }

      if (Files.exists(newFile)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST;
      }

      try {
        Files.move(file, newFile);
        arguments.setRenamed(true);
        renameThumb(arguments, configuration);
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE;
      } catch (IOException ex) {
        arguments.setRenamed(false);
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
  private void renameThumb(RenameFileArguments arguments, IConfiguration configuration) throws IOException {
    Path thumbFile = Paths.get(configuration.getThumbsPath(),
            arguments.getType().getName(), arguments.getCurrentFolder(),
            arguments.getFileName());
    Path newThumbFile = Paths.get(configuration.getThumbsPath(),
            arguments.getType().getName(), arguments.getCurrentFolder(),
            arguments.getNewFileName());

    try {
      Files.move(thumbFile, newThumbFile);
    } catch (IOException ex) {
    }
  }

  @Override
  protected void initParams(RenameFileArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);
    arguments.setFileName(request.getParameter("fileName"));
    arguments.setNewFileName(request.getParameter("newFileName"));
  }

}
