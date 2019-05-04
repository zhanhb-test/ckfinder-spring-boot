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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFolderParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFolder;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
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
   * @param context ckfinder context
   * @throws ConnectorException when error occurs
   */
  @Override
  protected void createXml(Connector.Builder rootElement, RenameFolderParameter param, CKFinderContext context) throws ConnectorException {
    checkRequestPath(param.getNewFolderName());
    CommandContext cmdContext = param.getContext();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FOLDER_RENAME);

    if (context.isForceAscii()) {
      param.setNewFolderName(FileUtils.convertToAscii(param.getNewFolderName()));
    }

    if (context.isDirectoryHidden(param.getNewFolderName())
            || FileUtils.isFolderNameInvalid(param.getNewFolderName(), context)) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }

    if (cmdContext.getCurrentFolder().equals("/")) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(cmdContext.getType().getPath(),
            cmdContext.getCurrentFolder());
    if (!Files.isDirectory(dir)) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }
    setNewFolder(param);
    Path newDir = getPath(cmdContext.getType().getPath(),
            param.getNewFolderPath());
    if (Files.exists(newDir)) {
      cmdContext.throwException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.move(dir, newDir);
      renameThumb(param);
    } catch (IOException ex) {
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
    rootElement.result(RenamedFolder.builder()
            .newName(param.getNewFolderName())
            .newPath(param.getNewFolderPath())
            .newUrl(cmdContext.getType().getUrl() + param.getNewFolderPath())
            .build());
  }

  /**
   * renames thumb folder.
   *
   * @param param the parameter
   */
  private void renameThumb(RenameFolderParameter param) {
    CommandContext cmdContext = param.getContext();
    Path thumbnailPath = cmdContext.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path thumbDir = getPath(thumbnailPath, cmdContext.getCurrentFolder());
      Path newThumbDir = getPath(cmdContext.getType().getThumbnailPath(), param.getNewFolderPath());
      try {
        Files.move(thumbDir, newThumbDir);
      } catch (IOException ignored) {
      }
    }
  }

  /**
   * sets new folder name.
   *
   * @param param the parameter
   */
  private void setNewFolder(RenameFolderParameter param) {
    CommandContext cmdContext = param.getContext();
    String str = cmdContext.getCurrentFolder();
    int index = str.lastIndexOf('/', str.lastIndexOf('/') - 1);
    String path = PathUtils.addSlashToEnd(str.substring(0, index + 1).concat(param.getNewFolderName()));
    param.setNewFolderPath(path);
  }

  /**
   * @param request request
   * @param context ckfinder context
   * @return the parameter
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected RenameFolderParameter popupParams(HttpServletRequest request, CKFinderContext context) throws ConnectorException {
    RenameFolderParameter param = doInitParam(new RenameFolderParameter(), request, context);
    param.setNewFolderName(request.getParameter("NewFolderName"));
    return param;
  }

}
