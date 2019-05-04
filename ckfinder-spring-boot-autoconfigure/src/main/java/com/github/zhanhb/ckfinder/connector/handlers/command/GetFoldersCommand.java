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
import com.github.zhanhb.ckfinder.connector.handlers.response.Folder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Folders;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>GetFolders</code> command. Get subfolders for selected
 * location command.
 */
@Slf4j
public class GetFoldersCommand extends BaseXmlCommand<Void> {

  @Override
  protected void createXml(Connector.Builder rootElement, Void param, CommandContext cmdContext) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FOLDER_VIEW);

    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      cmdContext.throwException(ErrorCode.FOLDER_NOT_FOUND);
    }

    try {
      List<Path> directories = FileUtils.listChildren(dir, true);
      createFoldersData(rootElement, cmdContext, directories);
    } catch (IOException e) {
      log.error("", e);
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * creates folder data node in XML document.
   *
   * @param rootElement root element in XML document
   * @param cmdContext command context
   * @param directories list of children folder
   */
  private void createFoldersData(Connector.Builder rootElement, CommandContext cmdContext, List<Path> directories) {
    CKFinderContext context = cmdContext.getCfCtx();
    Folders.Builder folders = Folders.builder();
    for (Path dir : directories) {
      String dirName = dir.getFileName().toString();
      if (!context.getAccessControl().hasPermission(cmdContext.getType().getName(), cmdContext.getCurrentFolder() + dirName, cmdContext.getUserRole(),
              AccessControl.FOLDER_VIEW)) {
        continue;
      }
      if (context.isDirectoryHidden(dirName)) {
        continue;
      }
      boolean hasChildren = FileUtils.hasChildren(context.getAccessControl(),
              cmdContext.getCurrentFolder() + dirName + "/", dir,
              context, cmdContext.getType().getName(), cmdContext.getUserRole());

      folders.folder(Folder.builder()
              .name(dirName)
              .hasChildren(hasChildren)
              .acl(context.getAccessControl()
                      .getAcl(cmdContext.getType().getName(),
                              cmdContext.getCurrentFolder()
                              + dirName, cmdContext.getUserRole())).build());
    }
    rootElement.result(folders.build());
  }

  @Override
  protected Void popupParams(HttpServletRequest request, CKFinderContext context) {
    return null;
  }

}
