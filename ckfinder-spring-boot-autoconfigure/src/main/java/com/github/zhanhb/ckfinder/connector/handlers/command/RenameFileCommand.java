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
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFile;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
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
  protected void addResultNode(Connector.Builder rootElement, RenameFileParameter param) {
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
   * @param context ckfinder context
   * @return error code or null if it's correct.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected ErrorCode getDataForXml(RenameFileParameter param, CommandContext cmdContext)
          throws ConnectorException {
    log.trace("getDataForXml");
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME);

    if (context.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToAscii(param.getNewFileName()));
    }

    if (param.getFileName() != null && !param.getFileName().isEmpty()
            && param.getNewFileName() != null && !param.getNewFileName().isEmpty()) {
      param.setAddResultNode(true);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getNewFileName(), cmdContext.getType())) {
      return ErrorCode.INVALID_EXTENSION;
    }
    if (context.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(cmdContext.getType(),
              param.getNewFileName()));
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || context.isFileHidden(param.getFileName())) {
      return ErrorCode.INVALID_REQUEST;
    }

    if (!FileUtils.isFileNameValid(param.getNewFileName(), context)
            || context.isFileHidden(param.getNewFileName())) {
      return ErrorCode.INVALID_NAME;
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(),
            cmdContext.getType())) {
      return ErrorCode.INVALID_REQUEST;
    }

    Path dirPath = cmdContext.getType().getPath();
    Path file = getPath(dirPath, cmdContext.getCurrentFolder(), param.getFileName());
    Path newFile = getPath(dirPath, cmdContext.getCurrentFolder(), param.getNewFileName());

    try {
      Files.move(file, newFile);
      param.setRenamed(true);
      renameThumb(param, cmdContext);
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
   */
  private void renameThumb(RenameFileParameter param, CommandContext cmdContext) {
    Path thumbnailPath = cmdContext.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path thumbFile = getPath(thumbnailPath, cmdContext.getCurrentFolder(),
              param.getFileName());
      Path newThumbFile = getPath(cmdContext.getType().getThumbnailPath(), cmdContext.getCurrentFolder(),
              param.getNewFileName());

      try {
        Files.move(thumbFile, newThumbFile);
      } catch (IOException ignored) {
      }
    }
  }

  @Override
  protected RenameFileParameter popupParams(HttpServletRequest request, CKFinderContext context)
          throws ConnectorException {
    RenameFileParameter param = new RenameFileParameter();
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    return param;
  }

}
