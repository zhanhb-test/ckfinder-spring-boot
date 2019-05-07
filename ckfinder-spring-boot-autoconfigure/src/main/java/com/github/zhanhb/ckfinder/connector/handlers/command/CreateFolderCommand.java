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
import com.github.zhanhb.ckfinder.connector.handlers.response.NewFolder;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CreateFolder</code> command. Create subfolder.
 */
@Slf4j
public class CreateFolderCommand extends FinishOnErrorXmlCommand<String> implements IPostCommand {

  /**
   * creates current folder XML node.
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
    cmdContext.checkAllPermission(AccessControl.FOLDER_CREATE);

    if (context.isForceAscii()) {
      newFolderName = FileUtils.convertToAscii(newFolderName);
    }

    if (FileUtils.isFolderNameInvalid(newFolderName, context)) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }
    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }
    if (context.isDirectoryHidden(newFolderName)) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }

    Path dir = cmdContext.resolve(newFolderName);
    if (Files.exists(dir)) {
      cmdContext.throwException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      cmdContext.throwException(ErrorCode.UNAUTHORIZED);
    }

    rootElement.result(NewFolder.builder().name(newFolderName).build());
  }

  @Override
  protected String popupParams(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("NewFolderName");
  }

}
