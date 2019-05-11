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
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Class to handle <code>RenameFile</code> command.
 */
@Slf4j
public class RenameFileCommand extends FailAtEndXmlCommand<RenameFileParameter> implements IPostCommand {

  /**
   * rename thumb file.
   *
   * @param param the parameter
   */
  private void renameThumb(RenameFileParameter param, CommandContext cmdContext) {
    cmdContext.resolveThumbnail(param.getFileName()).ifPresent(thumbFile -> {
      cmdContext.resolveThumbnail(param.getNewFileName()).ifPresent(newThumbFile -> {
        try {
          log.debug("remove thumb '{}'->'{}'", thumbFile, newThumbFile);
          Files.move(thumbFile, newThumbFile);
        } catch (IOException ignored) {
        }
      });
    });
  }

  /**
   * gets data for XML and checks all validation.
   *
   * @param param the parameter
   * @param cmdContext command context
   * @return error code or null if it's correct.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected ErrorListResult applyData(RenameFileParameter param, CommandContext cmdContext)
          throws ConnectorException {
    log.trace("getDataForXml");
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME);

    String fileName = param.getFileName();
    String newFileName = param.getNewFileName();

    if (context.isForceAscii()) {
      newFileName = FileUtils.convertToAscii(newFileName);
      param.setNewFileName(newFileName);
    }

    ErrorListResult.Builder builder = ErrorListResult.builder();

    if (!StringUtils.isEmpty(fileName) && !StringUtils.isEmpty(newFileName)) {
      builder.addResultNode(true);
    }

    if (!FileUtils.isFileExtensionAllowed(newFileName, cmdContext.getType())) {
      return builder.errorCode(ErrorCode.INVALID_EXTENSION).build();
    }
    if (!context.isDoubleFileExtensionsAllowed()) {
      newFileName = FileUtils.renameFileWithBadExt(cmdContext.getType(), newFileName);
      param.setNewFileName(newFileName);
    }

    if (!FileUtils.isFileNameValid(fileName) || context.isFileHidden(fileName)) {
      return builder.errorCode(ErrorCode.INVALID_REQUEST).build();
    }

    if (!FileUtils.isFileNameValid(newFileName, context) || context.isFileHidden(newFileName)) {
      return builder.errorCode(ErrorCode.INVALID_NAME).build();
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      return builder.errorCode(ErrorCode.INVALID_REQUEST).build();
    }

    Path file = cmdContext.resolve(fileName);
    Path newFile = cmdContext.resolve(newFileName);

    try {
      Files.move(file, newFile);
      param.setRenamed(true);
      renameThumb(param, cmdContext);
      return builder.build();
    } catch (NoSuchFileException ex) {
      return builder.errorCode(ErrorCode.FILE_NOT_FOUND).build();
    } catch (FileAlreadyExistsException ex) {
      return builder.errorCode(ErrorCode.ALREADY_EXIST).build();
    } catch (IOException ex) {
      param.setRenamed(false);
      log.error("IOException", ex);
      return builder.errorCode(ErrorCode.ACCESS_DENIED).build();
    }
  }

  @Override
  protected void addResultNode(Connector.Builder rootElement, RenameFileParameter param) {
    RenamedFile.Builder element = RenamedFile.builder().name(param.getFileName());
    if (param.isRenamed()) {
      element.newName(param.getNewFileName());
    }
    rootElement.result(element.build());
  }

  @Override
  protected RenameFileParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    RenameFileParameter param = new RenameFileParameter();
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    return param;
  }

}
