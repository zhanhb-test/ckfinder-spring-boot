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
import com.github.zhanhb.ckfinder.download.ContentDispositionStrategy;
import com.github.zhanhb.ckfinder.download.ContentTypeResolver;
import com.github.zhanhb.ckfinder.download.PathPartial;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

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

    if (FileUtils.isFileNameInvalid(fileName)
            || !FileUtils.isFileExtensionAllowed(fileName,
                    cmdContext.getType())) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }
    if (context.isFileHidden(fileName)) {
      throw cmdContext.toException(ErrorCode.FILE_NOT_FOUND);
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
      throw new ConnectorException(ErrorCode.UNKNOWN, ex);
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
  protected String parseParameters(HttpServletRequest request, CKFinderContext context) {
    // problem with showing filename when dialog window appear
    return request.getParameter("FileName");
  }

  private interface PartialHolder {

    PathPartial INSTANCE = PathPartial.builder()
            .contentType(context -> Optional.ofNullable(ContentTypeResolver.getDefault().apply(context)).map(mt
            -> mt.matches("(?i)^text/.+|.+[/\\+](?:html|javascript|xml)$")
            ? MediaType.TEXT_PLAIN_VALUE : mt).orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE))
            .notFound(__ -> {
              throw new UncheckedConnectorException(ErrorCode.FILE_NOT_FOUND);
            })
            .contentDisposition(ContentDispositionStrategy.attachment())
            .build();
  }

}
