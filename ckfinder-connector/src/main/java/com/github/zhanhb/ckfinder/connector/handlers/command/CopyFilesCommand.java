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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CopyMoveParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.CopyFilesElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;
import com.github.zhanhb.ckfinder.connector.support.FileItem;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CopyFiles</code> command.
 */
@Slf4j
public class CopyFilesCommand extends FailAtEndXmlCommand<CopyMoveParameter> implements IPostCommand {

  @Override
  protected void createXml(CopyMoveParameter param, CommandContext cmdContext, ConnectorElement.Builder rootElement)
          throws ConnectorException {
    cmdContext.checkType();
    List<FileItem> files = param.getFiles();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME
            | AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD);
    cmdContext.checkFilePostParam(files, AccessControl.FILE_VIEW);

    ErrorListResult.Builder builder = ErrorListResult.builder();
    int success = CopyMoveHelper.process(cmdContext, files, builder, Files::copy, "copy");
    builder.ifError(ErrorCode.COPY_FAILED).addErrorsTo(rootElement);
    rootElement.result(CopyFilesElement.builder()
            .copied(success)
            .copiedTotal(param.getAll() + success)
            .build());
  }

  @Override
  protected CopyMoveParameter parseParameters(HttpServletRequest request, CKFinderContext context) {
    String copied = request.getParameter("copied");
    int all = copied != null ? Integer.parseInt(copied) : 0;
    List<FileItem> files = RequestFileHelper.getFilesList(request, context);
    return CopyMoveParameter.builder().files(files).all(all).build();
  }

}
