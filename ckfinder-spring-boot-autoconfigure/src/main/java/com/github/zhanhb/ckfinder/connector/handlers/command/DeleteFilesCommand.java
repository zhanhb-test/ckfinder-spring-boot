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
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class used to handle <code>DeleteFiles</code> command.
 */
@Slf4j
public class DeleteFilesCommand extends ErrorListXmlCommand<DeleteFilesParameter> implements IPostCommand {

  public DeleteFilesCommand() {
    super(DeleteFilesParameter::new);
  }

  @Override
  protected void addRestNodes(Connector.Builder rootElement, DeleteFilesParameter param, IConfiguration configuration) {
    if (param.isAddDeleteNode()) {
      createDeleteFielsNode(rootElement, param);
    }
  }

  /**
   * Adds file deletion node in XML response.
   *
   * @param rootElement root element in XML response
   * @param param
   */
  private void createDeleteFielsNode(Connector.Builder rootElement, DeleteFilesParameter param) {
    rootElement.deleteFiles(DeleteFiles.builder()
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

    param.setFilesDeleted(0);

    param.setAddDeleteNode(false);

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

      if (FileUtils.isDirectoryHidden(fileItem.getFolder(), configuration)) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (FileUtils.isFileHidden(fileItem.getName(), configuration)) {
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
      Path file = Paths.get(fileItem.getType().getPath(), fileItem.getFolder(), fileItem.getName());

      try {
        param.setAddDeleteNode(true);
        if (!Files.exists(file)) {
          param.appendErrorNodeChild(ConnectorError.FILE_NOT_FOUND,
                  fileItem.getName(), fileItem.getFolder(), fileItem.getType().getName());
          continue;
        }

        log.debug("prepare delete file '{}'", file);
        if (FileUtils.delete(file)) {
          Path thumbFile = Paths.get(configuration.getThumbsPath(),
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
          param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                  fileItem.getName(), fileItem.getFolder(), fileItem.getType().getName());
        }
      } catch (SecurityException e) {
        log.error("", e);
        return ConnectorError.ACCESS_DENIED;
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
   * @param param
   * @param request current response object
   * @param configuration connector configuration object
   * @throws ConnectorException when initialization parameters can't be loaded
   * for command handler.
   */
  @Override
  protected void initParams(DeleteFilesParameter param, HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    super.initParams(param, request, configuration);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), configuration);
  }

}
