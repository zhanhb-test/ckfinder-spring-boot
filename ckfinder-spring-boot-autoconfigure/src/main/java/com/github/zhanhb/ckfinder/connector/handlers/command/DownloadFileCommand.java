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

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.DownloadFileArguments;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to handle <code>DownloadFile</code> command.
 */
public class DownloadFileCommand extends Command<DownloadFileArguments> {

  public DownloadFileCommand() {
    super(DownloadFileArguments::new);
  }

  /**
   * executes the download file command. Writes file to response.
   *
   * @throws ConnectorException when something went wrong during reading file.
   */
  @Override
  void execute(DownloadFileArguments arguments, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration)
          throws ConnectorException {
    if (arguments.getType() == null) {
      throw new ConnectorException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    Path file = Paths.get(arguments.getType().getPath(), arguments.getCurrentFolder(), arguments.getFileName());

    if (file != null) {
      try {
        response.setContentLengthLong(Files.size(file));
      } catch (IOException ex) {
      }
    }

    String mimetype = request.getServletContext().getMimeType(arguments.getFileName());
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
                    arguments.getFileName()));

    response.setHeader("Cache-Control", "cache, must-revalidate");
    response.setHeader("Pragma", "public");
    response.setHeader("Expires", "0");

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameInvalid(arguments.getFileName())
            || !FileUtils.isFileExtensionAllwed(arguments.getFileName(),
                    arguments.getType())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (FileUtils.isDirectoryHidden(arguments.getCurrentFolder(), configuration)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }
    try {
      if (!Files.exists(file)
              || !Files.isRegularFile(file)
              || FileUtils.isFileHidden(arguments.getFileName(), configuration)) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }

      FileUtils.printFileContentToResponse(file, response.getOutputStream());
    } catch (IOException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }

  }

  /**
   * inits params for download file command.
   *
   * @param arguments
   * @param request request
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs.
   */
  @Override
  protected void initParams(DownloadFileArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);
    // problem with showing filename when dialog window appear
    arguments.setFileName(request.getParameter("FileName"));
  }

}
