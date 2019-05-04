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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ErrorListXmlParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>DeleteFolder</code> command.
 */
@Slf4j
public class DeleteFolderCommand extends BaseXmlCommand<ErrorListXmlParameter> implements IPostCommand {

  @Override
  protected void createXml(Connector.Builder rootElement, ErrorListXmlParameter param,
          CommandContext cmdContext) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FOLDER_DELETE);

    if (cmdContext.getCurrentFolder().equals("/")) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      cmdContext.throwException(ErrorCode.FOLDER_NOT_FOUND);
    }

    if (FileUtils.delete(dir)) {
      Path thumbnailPath = cmdContext.getType().getThumbnailPath();
      if (thumbnailPath != null) {
        Path thumbDir = getPath(thumbnailPath, cmdContext.getCurrentFolder());
        FileUtils.delete(thumbDir);
      }
    } else {
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  @Override
  protected ErrorListXmlParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    return new ErrorListXmlParameter();
  }

}
