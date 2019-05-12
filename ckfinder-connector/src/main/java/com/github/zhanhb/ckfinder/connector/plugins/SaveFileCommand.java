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
package com.github.zhanhb.ckfinder.connector.plugins;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.command.FinishOnErrorXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.SaveFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SaveFileCommand extends FinishOnErrorXmlCommand<SaveFileParameter> implements IPostCommand {

  @Override
  protected void createXml(Connector.Builder rootElement, SaveFileParameter param,
          CommandContext cmdContext) throws ConnectorException {
    cmdContext.checkType();

    cmdContext.checkAllPermission(AccessControl.FILE_DELETE);

    String fileName = param.getFileName();
    String content = param.getFileContent();

    if (fileName == null || fileName.isEmpty()) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }

    if (content == null || content.isEmpty()) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      cmdContext.throwException(ErrorCode.INVALID_EXTENSION);
    }

    if (!FileUtils.isFileNameValid(fileName)) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path sourceFile = cmdContext.resolve(fileName);

    if (!Files.isRegularFile(sourceFile)) {
      cmdContext.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    try {
      Files.write(sourceFile, content.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      log.error("", e);
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  @Override
  protected SaveFileParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    return SaveFileParameter.builder()
            .fileContent(request.getParameter("content"))
            .fileName(request.getParameter("fileName"))
            .build();
  }

}
