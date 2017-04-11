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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFile;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>RenameFile</code> command.
 */
@Slf4j
public class RenameFileCommand extends ErrorListXmlCommand<RenameFileParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, RenameFileParameter param, Configuration configuration) {
    RenamedFile.Builder element = RenamedFile.builder().name(param.getFileName());
    if (param.isRenamed()) {
      element.newName(param.getNewFileName());
    }
    rootElement.result(element.build());
  }

  /**
   * gets data for XML and checks all validation.
   *
   * @param param the parameter
   * @param configuration connector configuration
   * @return error code or null if it's correct.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected ErrorCode getDataForXml(RenameFileParameter param, Configuration configuration)
          throws ConnectorException {
    log.trace("getDataForXml");
    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_RENAME)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToAscii(param.getNewFileName()));
    }

    if (param.getFileName() != null && !param.getFileName().isEmpty()
            && param.getNewFileName() != null && !param.getNewFileName().isEmpty()) {
      param.setAddResultNode(true);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getNewFileName(), param.getType())) {
      return ErrorCode.INVALID_EXTENSION;
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(param.getType(),
              param.getNewFileName()));
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || configuration.isFileHidden(param.getFileName())) {
      return ErrorCode.INVALID_REQUEST;
    }

    if (!FileUtils.isFileNameValid(param.getNewFileName(), configuration)
            || configuration.isFileHidden(param.getNewFileName())) {
      return ErrorCode.INVALID_NAME;
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(),
            param.getType())) {
      return ErrorCode.INVALID_REQUEST;
    }

    Path dirPath = param.getType().getPath();
    Path file = getPath(dirPath, param.getCurrentFolder(), param.getFileName());
    Path newFile = getPath(dirPath, param.getCurrentFolder(), param.getNewFileName());

    try {
      Files.move(file, newFile);
      param.setRenamed(true);
      renameThumb(param);
      return null;
    } catch (NoSuchFileException ex) {
      return ErrorCode.FILE_NOT_FOUND;
    } catch (FileAlreadyExistsException ex) {
      return ErrorCode.ALREADY_EXIST;
    } catch (IOException ex) {
      param.setRenamed(false);
      log.error("IOException", ex);
      return ErrorCode.ACCESS_DENIED;
    }

  }

  /**
   * rename thumb file.
   *
   * @param param the parameter
   * @throws IOException when IO Exception occurs.
   */
  private void renameThumb(RenameFileParameter param) throws IOException {
    Path thumbnailPath = param.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path thumbFile = getPath(thumbnailPath, param.getCurrentFolder(),
              param.getFileName());
      Path newThumbFile = getPath(param.getType().getThumbnailPath(), param.getCurrentFolder(),
              param.getNewFileName());

      try {
        Files.move(thumbFile, newThumbFile);
      } catch (IOException ex) {
      }
    }
  }

  @Override
  protected RenameFileParameter popupParams(HttpServletRequest request, Configuration configuration)
          throws ConnectorException {
    RenameFileParameter param = doInitParam(new RenameFileParameter(), request, configuration);
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    return param;
  }

}
