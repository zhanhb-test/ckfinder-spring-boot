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
import com.github.zhanhb.ckfinder.connector.data.FilePostParam;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.DeleteFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DeleteFiles;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
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
  protected void addResultNode(Connector.Builder rootElement, DeleteFilesParameter param, IConfiguration configuration) {
    rootElement.result(DeleteFiles.builder()
            .deleted(param.getFilesDeleted())
            .build());
  }

  /**
   * Prepares data for XML response.
   *
   * @param param
   * @param configuration connector configuration
   * @return error code or 0 if action ended with success.
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected ConnectorError getDataForXml(DeleteFilesParameter param, IConfiguration configuration)
          throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    for (FilePostParam fileItem : param.getFiles()) {
      if (!FileUtils.isFileNameValid(fileItem.getName())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (fileItem.getType() == null) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (fileItem.getFolder() == null || fileItem.getFolder().isEmpty()
              || Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
                      fileItem.getFolder()).find()) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (configuration.isDirectoryHidden(fileItem.getFolder())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (configuration.isFileHidden(fileItem.getName())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (!FileUtils.isFileExtensionAllowed(fileItem.getName(), fileItem.getType())) {
        param.throwException(ConnectorError.INVALID_REQUEST);

      }

      if (!configuration.getAccessControl().hasPermission(fileItem.getType().getName(), fileItem.getFolder(), param.getUserRole(),
              AccessControl.FILE_DELETE)) {
        param.throwException(ConnectorError.UNAUTHORIZED);
      }
    }

    for (FilePostParam fileItem : param.getFiles()) {
      Path file = getPath(fileItem.getType().getPath(), fileItem.getFolder(), fileItem.getName());

      param.setAddResultNode(true);
      if (!Files.exists(file)) {
        param.appendError(fileItem, ConnectorError.FILE_NOT_FOUND);
        continue;
      }

      log.debug("prepare delete file '{}'", file);
      if (FileUtils.delete(file)) {
        Path thumbFile = getPath(configuration.getThumbsPath(),
                fileItem.getType().getName(), param.getCurrentFolder(), fileItem.getName());
        param.filesDeletedPlus();

        try {
          log.debug("prepare delete thumb file '{}'", thumbFile);
          FileUtils.delete(thumbFile);
        } catch (Exception ignore) {
          log.debug("delete thumb file '{}' failed", thumbFile);
          // No errors if we are not able to delete the thumb.
        }
      } else { //If access is denied, report error and try to delete rest of files.
        param.appendError(fileItem, ConnectorError.ACCESS_DENIED);
      }
    }
    if (param.hasError()) {
      return ConnectorError.DELETE_FAILED;
    } else {
      return null;
    }
  }

  /**
   * Initializes parameters for command handler.
   *
   * @param request current response object
   * @param configuration connector configuration object
   * @return
   * @throws ConnectorException when initialization parameters can't be loaded
   * for command handler.
   */
  @Override
  protected DeleteFilesParameter popupParams(HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    DeleteFilesParameter param = doInitParam(new DeleteFilesParameter(), request, configuration);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), configuration);
    return param;
  }

}
