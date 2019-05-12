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
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.NewFolderElement;
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
  protected void createXml(String newFolderName, CommandContext cmdContext,
          ConnectorElement.Builder rootElement) throws ConnectorException {
    checkRequestPath(newFolderName);

    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FOLDER_CREATE);

    if (context.isForceAscii()) {
      newFolderName = FileUtils.convertToAscii(newFolderName);
    }

    if (FileUtils.isFolderNameInvalid(newFolderName, context)) {
      throw cmdContext.toException(ErrorCode.INVALID_NAME);
    }
    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }
    if (context.isDirectoryHidden(newFolderName)) {
      throw cmdContext.toException(ErrorCode.INVALID_NAME);
    }

    Path dir = cmdContext.resolve(newFolderName);
    if (Files.exists(dir)) {
      throw cmdContext.toException(ErrorCode.ALREADY_EXIST);
    }
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      throw cmdContext.toException(ErrorCode.UNAUTHORIZED);
    }

    rootElement.result(NewFolderElement.builder().name(newFolderName).build());
  }

  @Override
  protected String parseParameters(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("NewFolderName");
  }

}
