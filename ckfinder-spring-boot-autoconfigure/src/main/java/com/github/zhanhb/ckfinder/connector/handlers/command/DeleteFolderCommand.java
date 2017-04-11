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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ErrorListXmlParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
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
  protected void createXml(Connector.Builder rootElement, ErrorListXmlParameter param, Configuration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.FOLDER_DELETE)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }
    if (param.getCurrentFolder().equals("/")) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path dir = getPath(param.getType().getPath(), param.getCurrentFolder());

    if (!Files.isDirectory(dir)) {
      param.throwException(ErrorCode.FOLDER_NOT_FOUND);
    }

    if (FileUtils.delete(dir)) {
      Path thumbnailPath = param.getType().getThumbnailPath();
      if (thumbnailPath != null) {
        Path thumbDir = getPath(thumbnailPath, param.getCurrentFolder());
        FileUtils.delete(thumbDir);
      }
    } else {
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  @Override
  protected ErrorListXmlParameter popupParams(HttpServletRequest request, Configuration configuration) throws ConnectorException {
    return doInitParam(new ErrorListXmlParameter(), request, configuration);
  }

}
