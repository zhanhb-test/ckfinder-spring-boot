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
public class RenameFolderCommand extends SuccessXmlCommand<String> implements IPostCommand {

  /**
   * creates XML node for renamed folder.
   *
   * @param rootElement XML root element.
   * @param cmdContext command context
   * @throws ConnectorException when error occurs
   */
  @Override
  @SuppressWarnings("AssignmentToMethodParameter")
  protected void createXml(Connector.Builder rootElement, String newFolderName, CommandContext cmdContext) throws ConnectorException {
    checkRequestPath(newFolderName);
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FOLDER_RENAME);

    if (context.isForceAscii()) {
      newFolderName = FileUtils.convertToAscii(newFolderName);
    }

    if (context.isDirectoryHidden(newFolderName)
            || FileUtils.isFolderNameInvalid(newFolderName, context)) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }

    if (cmdContext.getCurrentFolder().equals("/")) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = cmdContext.toPath();
    if (!Files.isDirectory(dir)) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }
    String newFolderPath = toNewFolder(newFolderName, cmdContext);
    Path newDir = cmdContext.getType().resolve(newFolderPath);
    if (Files.exists(newDir)) {
      cmdContext.throwException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.move(dir, newDir);
      renameThumb(newFolderPath, cmdContext);
    } catch (IOException ex) {
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
    rootElement.result(RenamedFolder.builder()
            .newName(newFolderName)
            .newPath(newFolderPath)
            .newUrl(cmdContext.getType().getUrl() + newFolderPath)
            .build());
  }

  /**
   * renames thumb folder.
   */
  private void renameThumb(String newFolderPath, CommandContext cmdContext) {
    cmdContext.toThumbnail().ifPresent(thumbDir -> {
      cmdContext.resolveThumbnail(newFolderPath).ifPresent(newThumbDir -> {
        try {
          Files.move(thumbDir, newThumbDir);
        } catch (IOException ignored) {
        }
      });
    });
  }

  /**
   * calculate new folder path.
   */
  private String toNewFolder(String newFolderName, CommandContext cmdContext) {
    String str = cmdContext.getCurrentFolder();
    int index = str.lastIndexOf('/', str.lastIndexOf('/') - 1);
    return PathUtils.addSlashToEnd(str.substring(0, index + 1).concat(newFolderName));
  }

  /**
   * @param request request
   * @param context ckfinder context
   * @return the parameter
   */
  @Override
  protected String popupParams(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("NewFolderName");
  }

}
