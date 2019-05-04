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
public class DownloadFileCommand extends BaseCommand<String> {

  /**
   * executes the download file command. Writes file to response.
   *
   * @param fileName the fileName
   * @param request request
   * @throws ConnectorException when something went wrong during reading file.
   * @throws IOException when IO Exception occurs.
   */
  @Override
  void execute(String fileName, HttpServletRequest request, HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException {
    CommandContext cmdContext = populateCommandContext(request, context);
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_VIEW);

    if (!FileUtils.isFileNameValid(fileName)
            || !FileUtils.isFileExtensionAllowed(fileName,
                    cmdContext.getType())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isFileHidden(fileName)) {
      cmdContext.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    Path file = cmdContext.resolve(fileName);

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
   */
  @Override
  protected String popupParams(HttpServletRequest request, CKFinderContext context) {
    // problem with showing filename when dialog window appear
    return request.getParameter("FileName");
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
