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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.Folder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Folders;
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
public class GetFoldersCommand extends BaseXmlCommand<Parameter> {

  @Override
  protected void createXml(Connector.Builder rootElement, Parameter param, CKFinderContext context) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!context.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FOLDER_VIEW)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }
    if (context.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(param.getType().getPath(), param.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      param.throwException(ErrorCode.FOLDER_NOT_FOUND);
    }

    try {
      List<Path> directories = FileUtils.listChildren(dir, true);
      createFoldersData(rootElement, param, context, directories);
    } catch (IOException e) {
      log.error("", e);
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * creates folder data node in XML document.
   *
   * @param rootElement root element in XML document
   * @param param the parameter
   * @param context ckfinder context
   * @param directories list of children folder
   */
  private void createFoldersData(Connector.Builder rootElement, Parameter param, CKFinderContext context, List<Path> directories) {
    Folders.Builder folders = Folders.builder();
    for (Path dir : directories) {
      String dirName = dir.getFileName().toString();
      if (!context.getAccessControl().hasPermission(param.getType().getName(), param.getCurrentFolder() + dirName, param.getUserRole(),
              AccessControl.FOLDER_VIEW)) {
        continue;
      }
      if (context.isDirectoryHidden(dirName)) {
        continue;
      }
      boolean hasChildren = FileUtils.hasChildren(context.getAccessControl(),
              param.getCurrentFolder() + dirName + "/", dir,
              context, param.getType().getName(), param.getUserRole());

      folders.folder(Folder.builder()
              .name(dirName)
              .hasChildren(hasChildren)
              .acl(context.getAccessControl()
                      .getAcl(param.getType().getName(),
                              param.getCurrentFolder()
                              + dirName, param.getUserRole())).build());
    }
    rootElement.result(folders.build());
  }

  @Override
  protected Parameter popupParams(HttpServletRequest request, CKFinderContext context) throws ConnectorException {
    return doInitParam(new Parameter(), request, context);
  }

}
