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

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.DownloadFileParameter;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to handle <code>DownloadFile</code> command.
 */
public class DownloadFileCommand extends Command<DownloadFileParameter> {

  /**
   * executes the download file command. Writes file to response.
   *
   * @param param
   * @param request
   * @throws ConnectorException when something went wrong during reading file.
   * @throws java.io.IOException
   */
  @Override
  void execute(DownloadFileParameter param, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration)
          throws ConnectorException, IOException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_VIEW)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || !FileUtils.isFileExtensionAllowed(param.getFileName(),
                    param.getType())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (configuration.isFileHidden(param.getFileName())) {
      param.throwException(ConnectorError.FILE_NOT_FOUND);
    }

    Path file = getPath(param.getType().getPath(), param.getCurrentFolder(), param.getFileName());

    long size;

    try {
      size = Files.size(file);
    } catch (IOException ex) {
      param.throwException(ConnectorError.FILE_NOT_FOUND);
      return;
    }

    response.setContentLengthLong(size);

    String mimetype = request.getServletContext().getMimeType(param.getFileName());
    if (mimetype != null) {
      if (mimetype.startsWith("text/") || mimetype.endsWith("/javascript")
              || mimetype.endsWith("/xml")) {
        mimetype += ";charset=UTF-8";
      }
      response.setContentType(mimetype);
    } else {
      response.setContentType("application/octet-stream");
    }
    response.setHeader("Content-Disposition",
            ContentDisposition.getContentDisposition("attachment",
                    param.getFileName()));

    response.setHeader("Cache-Control", "cache, must-revalidate");
    response.setHeader("Pragma", "public");
    response.setHeader("Expires", "0");

    Files.copy(file, response.getOutputStream());
  }

  /**
   * inits params for download file command.
   *
   * @param request request
   * @param configuration connector configuration
   * @return
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected DownloadFileParameter popupParams(HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    DownloadFileParameter param = doInitParam(new DownloadFileParameter(), request, configuration);
    // problem with showing filename when dialog window appear
    param.setFileName(request.getParameter("FileName"));
    return param;
  }

}
