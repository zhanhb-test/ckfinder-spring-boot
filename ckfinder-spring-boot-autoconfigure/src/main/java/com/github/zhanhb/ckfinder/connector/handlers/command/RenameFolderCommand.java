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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFolder;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>RenameFolder</code> command.
 */
@Slf4j
public class RenameFolderCommand extends BaseXmlCommand<RenameFolderParameter> implements IPostCommand {

  /**
   * creates XML node for renamed folder.
   *
   * @param rootElement XML root element.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected void createXml(Connector.Builder rootElement, RenameFolderParameter param, Configuration configuration) throws ConnectorException {
    checkRequestPathValid(param.getNewFolderName());

    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.FOLDER_RENAME)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToAscii(param.getNewFolderName()));
    }

    if (configuration.isDirectoryHidden(param.getNewFolderName())
            || !FileUtils.isFolderNameValid(param.getNewFolderName(), configuration)) {
      param.throwException(ErrorCode.INVALID_NAME);
    }

    if (param.getCurrentFolder().equals("/")) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(param.getType().getPath(),
            param.getCurrentFolder());
    if (!Files.isDirectory(dir)) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }
    setNewFolder(param);
    Path newDir = getPath(param.getType().getPath(),
            param.getNewFolderPath());
    if (Files.exists(newDir)) {
      param.throwException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.move(dir, newDir);
      renameThumb(param);
    } catch (IOException ex) {
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
    rootElement.result(RenamedFolder.builder()
            .newName(param.getNewFolderName())
            .newPath(param.getNewFolderPath())
            .newUrl(param.getType().getUrl() + param.getNewFolderPath())
            .build());
  }

  /**
   * renames thumb folder.
   *
   * @param param the parameter
   * @throws IOException when IO Exception occurs.
   */
  private void renameThumb(RenameFolderParameter param) throws IOException {
    Path thumbnailPath = param.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path thumbDir = getPath(thumbnailPath, param.getCurrentFolder());
      Path newThumbDir = getPath(param.getType().getThumbnailPath(), param.getNewFolderPath());
      try {
        Files.move(thumbDir, newThumbDir);
      } catch (IOException ex) {
      }
    }
  }

  /**
   * sets new folder name.
   *
   * @param param the parameter
   */
  private void setNewFolder(RenameFolderParameter param) {
    String str = param.getCurrentFolder();
    int index = str.lastIndexOf('/', str.lastIndexOf('/') - 1);
    String path = PathUtils.addSlashToEnd(str.substring(0, index + 1).concat(param.getNewFolderName()));
    param.setNewFolderPath(path);
  }

  /**
   * @param request request
   * @param configuration connector configuration
   * @return the parameter
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected RenameFolderParameter popupParams(HttpServletRequest request, Configuration configuration) throws ConnectorException {
    RenameFolderParameter param = doInitParam(new RenameFolderParameter(), request, configuration);
    param.setNewFolderName(request.getParameter("NewFolderName"));
    return param;
  }

}
