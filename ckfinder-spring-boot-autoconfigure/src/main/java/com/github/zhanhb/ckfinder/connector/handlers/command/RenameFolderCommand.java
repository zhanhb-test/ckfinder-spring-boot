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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFolder;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>RenameFolder</code> command.
 */
@Slf4j
public class RenameFolderCommand extends BaseXmlCommand<RenameFolderParameter> implements IPostCommand {

  public RenameFolderCommand() {
    super(RenameFolderParameter::new);
  }

  /**
   * creates XML node for renamed folder.
   *
   * @param rootElement XML root element.
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  protected void createXml(Connector.Builder rootElement, RenameFolderParameter param, IConfiguration configuration) throws ConnectorException {
    checkRequestPathValid(param.getNewFolderName());

    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.FOLDER_RENAME)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (configuration.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToAscii(param.getNewFolderName()));
    }

    if (configuration.isDirectoryHidden(param.getNewFolderName())
            || !FileUtils.isFolderNameValid(param.getNewFolderName(), configuration)) {
      param.throwException(ConnectorError.INVALID_NAME);
    }

    if (param.getCurrentFolder().equals("/")) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    Path dir = Paths.get(param.getType().getPath(),
            param.getCurrentFolder());
    if (!Files.isDirectory(dir)) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }
    setNewFolder(param);
    Path newDir = Paths.get(param.getType().getPath(),
            param.getNewFolderPath());
    if (Files.exists(newDir)) {
      param.throwException(ConnectorError.ALREADY_EXIST);
    }
    try {
      Files.move(dir, newDir);
      renameThumb(param, configuration);
    } catch (IOException ex) {
      param.throwException(ConnectorError.ACCESS_DENIED);
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
   * @param param
   * @param configuration
   * @throws java.io.IOException
   */
  private void renameThumb(RenameFolderParameter param, IConfiguration configuration) throws IOException {
    Path thumbDir = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder());
    Path newThumbDir = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getNewFolderPath());
    try {
      Files.move(thumbDir, newThumbDir);
    } catch (IOException ex) {
    }
  }

  /**
   * sets new folder name.
   *
   * @param param
   */
  private void setNewFolder(RenameFolderParameter param) {
    String tmp1 = param.getCurrentFolder().substring(0,
            param.getCurrentFolder().lastIndexOf('/'));
    param.setNewFolderPath(tmp1.substring(0,
            tmp1.lastIndexOf('/') + 1).concat(param.getNewFolderName()));
    param.setNewFolderPath(PathUtils.addSlashToEnd(param.getNewFolderPath()));
  }

  /**
   * @param param
   * @param request request
   * @param configuration connector conf
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected void initParams(RenameFolderParameter param, HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setNewFolderName(request.getParameter("NewFolderName"));
  }

}
