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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFile;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>RenameFile</code> command.
 */
@Slf4j
public class RenameFileCommand extends ErrorListXmlCommand<RenameFileParameter> implements IPostCommand {

  public RenameFileCommand() {
    super(RenameFileParameter::new);
  }

  @Override
  protected void addRestNodes(Connector.Builder rootElement, RenameFileParameter param, IConfiguration configuration) {
    if (param.isAddRenameNode()) {
      createRenamedFileNode(rootElement, param);
    }
  }

  /**
   * create rename file XML node.
   *
   * @param rootElement XML root node
   * @param param
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
  protected ConnectorError getDataForXml(RenameFileParameter param, IConfiguration configuration)
          throws ConnectorException {
    log.trace("getDataForXml");
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_RENAME)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToAscii(param.getNewFileName()));
    }

    if (param.getFileName() != null && !param.getFileName().isEmpty()
            && param.getNewFileName() != null && !param.getNewFileName().isEmpty()) {
      param.setAddRenameNode(true);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getNewFileName(), param.getType())) {
      return ConnectorError.INVALID_EXTENSION;
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(param.getType(),
              param.getNewFileName()));
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || configuration.isFileHidden(param.getFileName())) {
      return ConnectorError.INVALID_REQUEST;
    }

    if (!FileUtils.isFileNameInvalid(param.getNewFileName(), configuration)
            || configuration.isFileHidden(param.getNewFileName())) {
      return ConnectorError.INVALID_NAME;
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(),
            param.getType())) {
      return ConnectorError.INVALID_REQUEST;
    }

    String dirPath = param.getType().getPath();
    Path file = Paths.get(dirPath, param.getCurrentFolder(), param.getFileName());
    Path newFile = Paths.get(dirPath, param.getCurrentFolder(), param.getNewFileName());

    try {
      Files.move(file, newFile);
      param.setRenamed(true);
      renameThumb(param, configuration);
      return null;
    } catch (NoSuchFileException ex) {
      return ConnectorError.FILE_NOT_FOUND;
    } catch (FileAlreadyExistsException ex) {
      return ConnectorError.ALREADY_EXIST;
    } catch (IOException ex) {
      param.setRenamed(false);
      log.error("IOException", ex);
      return ConnectorError.ACCESS_DENIED;
    } catch (SecurityException e) {
      log.error("", e);
      return ConnectorError.ACCESS_DENIED;
    }

  }

  /**
   * rename thumb file.
   *
   * @param param
   * @param configuration
   * @throws java.io.IOException
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
