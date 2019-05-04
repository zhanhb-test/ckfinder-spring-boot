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
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.DeleteFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DeleteFiles;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to handle <code>DeleteFiles</code> command.
 */
@Slf4j
public class DeleteFilesCommand extends ErrorListXmlCommand<DeleteFilesParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, DeleteFilesParameter param, CKFinderContext context) {
    rootElement.result(DeleteFiles.builder()
            .deleted(param.getFilesDeleted())
            .build());
  }

  /**
   * Prepares data for XML response.
   *
   * @param param the parameter
   * @param context ckfinder context
   * @return error code or null if action ended with success.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected ErrorCode getDataForXml(DeleteFilesParameter param, CKFinderContext context)
          throws ConnectorException {
    CommandContext cmdContext = param.getContext();
    cmdContext.checkType();

    for (FilePostParam fileItem : param.getFiles()) {
      if (!FileUtils.isFileNameValid(fileItem.getName())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (fileItem.getType() == null) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (fileItem.getFolder() == null || fileItem.getFolder().isEmpty()
              || Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
                      fileItem.getFolder()).find()) {
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

      if (!context.getAccessControl().hasPermission(fileItem.getType().getName(), fileItem.getFolder(), cmdContext.getUserRole(),
              AccessControl.FILE_DELETE)) {
        cmdContext.throwException(ErrorCode.UNAUTHORIZED);
      }
    }

    for (FilePostParam fileItem : param.getFiles()) {
      Path file = getPath(fileItem.getType().getPath(), fileItem.getFolder(), fileItem.getName());

      param.setAddResultNode(true);
      if (!Files.exists(file)) {
        param.appendError(fileItem, ErrorCode.FILE_NOT_FOUND);
        continue;
      }

      log.debug("prepare delete file '{}'", file);
      if (FileUtils.delete(file)) {
        param.filesDeletedPlus();
        Path thumbnailPath = fileItem.getType().getThumbnailPath();
        if (thumbnailPath != null) {
          Path thumbFile = getPath(thumbnailPath, cmdContext.getCurrentFolder(), fileItem.getName());

          try {
            log.debug("prepare delete thumb file '{}'", thumbFile);
            FileUtils.delete(thumbFile);
          } catch (Exception ignore) {
            log.debug("delete thumb file '{}' failed", thumbFile);
            // No errors if we are not able to delete the thumb.
          }
        }
      } else { //If access is denied, report error and try to delete rest of files.
        param.appendError(fileItem, ErrorCode.ACCESS_DENIED);
      }
    }
    if (param.hasError()) {
      return ErrorCode.DELETE_FAILED;
    } else {
      return null;
    }
  }

  /**
   * Initializes parameters for command handler.
   *
   * @param request current response object
   * @param context ckfinder context object
   * @return the parameter
   * @throws ConnectorException when initialization parameters can't be loaded
   * for command handler.
   */
  @Override
  protected DeleteFilesParameter popupParams(HttpServletRequest request, CKFinderContext context) throws ConnectorException {
    DeleteFilesParameter param = doInitParam(new DeleteFilesParameter(), request, context);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), context);
    return param;
  }

}
