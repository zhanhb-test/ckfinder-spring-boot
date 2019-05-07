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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.DeleteFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DeleteFiles;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;
import com.github.zhanhb.ckfinder.connector.support.FileItem;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to handle <code>DeleteFiles</code> command.
 */
@Slf4j
public class DeleteFilesCommand extends FailAtEndXmlCommand<DeleteFilesParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, DeleteFilesParameter param) {
    rootElement.result(DeleteFiles.builder()
            .deleted(param.getFilesDeleted())
            .build());
  }

  /**
   * Prepares data for XML response.
   *
   * @param param the parameter
   * @param cmdContext command context
   * @return error code or null if action ended with success.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected ErrorListResult applyData(DeleteFilesParameter param, CommandContext cmdContext)
          throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();

    for (FileItem fileItem : param.getFiles()) {
      if (!FileUtils.isFileNameValid(fileItem.getName())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (fileItem.getType() == null) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (fileItem.getFolder() == null || fileItem.getFolder().isEmpty()
              || FileUtils.isPathNameInvalid(fileItem.getFolder())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (context.isDirectoryHidden(fileItem.getFolder())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (context.isFileHidden(fileItem.getName())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (!FileUtils.isFileExtensionAllowed(fileItem.getName(), fileItem.getType())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);

      }

      cmdContext.checkAllPermission(fileItem.getType(), fileItem.getFolder(), AccessControl.FILE_DELETE);
    }

    ErrorListResult.Builder builder = ErrorListResult.builder();

    for (FileItem fileItem : param.getFiles()) {
      Path file = fileItem.toPath();

      builder.addResultNode(true);
      if (!Files.exists(file)) {
        builder.appendError(fileItem, ErrorCode.FILE_NOT_FOUND);
        continue;
      }

      log.debug("prepare delete file '{}'", file);
      if (FileUtils.delete(file)) {
        param.filesDeletedPlus();
        fileItem.toThumbnailPath().ifPresent(thumbFile -> {
          try {
            log.debug("prepare delete thumb file '{}'", thumbFile);
            FileUtils.delete(thumbFile);
          } catch (Exception ignore) {
            log.info("delete thumb file '{}' failed", thumbFile);
            // No errors if we are not able to delete the thumb.
          }
        });
      } else { //If access is denied, report error and try to delete rest of files.
        builder.appendError(fileItem, ErrorCode.ACCESS_DENIED);
      }
    }
    return builder.ifError(ErrorCode.DELETE_FAILED);
  }

  /**
   * Initializes parameters for command handler.
   *
   * @param request current response object
   * @param context ckfinder context object
   * @return the parameter
   */
  @Override
  protected DeleteFilesParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    List<FileItem> files = RequestFileHelper.getFilesList(request, context);
    return new DeleteFilesParameter(files);
  }

}
