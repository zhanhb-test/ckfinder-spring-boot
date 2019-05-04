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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.DownloadFileParameter;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.download.ContentDisposition;
import com.github.zhanhb.ckfinder.download.ContentTypeResolver;
import com.github.zhanhb.ckfinder.download.PathPartial;
import java.io.IOException;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to handle <code>DownloadFile</code> command.
 */
public class DownloadFileCommand extends BaseCommand<DownloadFileParameter> {

  /**
   * executes the download file command. Writes file to response.
   *
   * @param param the parameter
   * @param request request
   * @throws ConnectorException when something went wrong during reading file.
   * @throws IOException when IO Exception occurs.
   */
  @Override
  void execute(DownloadFileParameter param, HttpServletRequest request, HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException {
    CommandContext cmdContext = param.getContext();
    cmdContext.checkType();

    if (!context.getAccessControl().hasPermission(cmdContext.getType().getName(),
            cmdContext.getCurrentFolder(), cmdContext.getUserRole(),
            AccessControl.FILE_VIEW)) {
      cmdContext.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || !FileUtils.isFileExtensionAllowed(param.getFileName(),
                    cmdContext.getType())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isFileHidden(param.getFileName())) {
      cmdContext.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    Path file = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder(), param.getFileName());

    response.setHeader("Cache-Control", "cache, must-revalidate");
    response.setHeader("Pragma", "public");
    response.setHeader("Expires", "0");

    try {
      PartialHolder.INSTANCE.service(request, response, file);
    } catch (UncheckedConnectorException ex) {
      throw ex.getCause();
    } catch (ServletException ex) {
      throw new AssertionError(ex);
    }
  }

  /**
   * inits params for download file command.
   *
   * @param request request
   * @param context ckfinder context
   * @return the parameter
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected DownloadFileParameter popupParams(HttpServletRequest request, CKFinderContext context)
          throws ConnectorException {
    DownloadFileParameter param = doInitParam(new DownloadFileParameter(), request, context);
    // problem with showing filename when dialog window appear
    param.setFileName(request.getParameter("FileName"));
    return param;
  }

  @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
  private static class PartialHolder {

    static PathPartial INSTANCE;

    static {
      ContentTypeResolver contentTypeResolver = ContentTypeResolver.getDefault();
      INSTANCE = PathPartial.builder()
              .contentType(context -> {
                String mimetype = contentTypeResolver.getValue(context);
                if (mimetype == null) {
                  return "application/octet-stream";
                }
                if (mimetype.startsWith("text/") || mimetype.endsWith("/javascript") || mimetype.endsWith("/xml")) {
                  return mimetype + ";charset=UTF-8";
                }
                return mimetype;
              })
              .notFound(context -> {
                throw new UncheckedConnectorException(ErrorCode.FILE_NOT_FOUND);
              })
              .contentDisposition(ContentDisposition.attachment())
              .build();
    }

  }

}
