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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ThumbnailParameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import com.github.zhanhb.ckfinder.download.ContentDisposition;
import com.github.zhanhb.ckfinder.download.PathPartial;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>Thumbnail</code> command. Get thumbnail for file
 * command.
 */
@Slf4j
public class ThumbnailCommand extends BaseCommand<ThumbnailParameter> {

  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(ThumbnailParameter param, HttpServletRequest request, HttpServletResponse response, CKFinderContext context) throws ConnectorException, IOException {
    if (context.getThumbnail() == null) {
      param.throwException(ErrorCode.THUMBNAILS_DISABLED);
    }

    if (param.getType() == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }

    if (!context.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_VIEW)) {
      param.throwException(ErrorCode.UNAUTHORIZED);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (context.isFileHidden(param.getFileName())) {
      param.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    Path fullCurrentPath = getPath(param.getType().getThumbnailPath(), param.getCurrentFolder());
    log.debug("typeThumbDir: {}", fullCurrentPath);

    try {
      log.debug("ThumbnailCommand.createThumb({})", fullCurrentPath);
      Files.createDirectories(fullCurrentPath);
    } catch (IOException e) {
      throw new ConnectorException(ErrorCode.ACCESS_DENIED, e);
    }
    Path thumbFile = getPath(fullCurrentPath, param.getFileName());
    log.debug("thumbFile: {}", thumbFile);

    if (!Files.exists(thumbFile)) {
      Path orginFile = getPath(param.getType().getPath(),
              param.getCurrentFolder(), param.getFileName());
      log.debug("orginFile: {}", orginFile);
      if (!Files.exists(orginFile)) {
        param.throwException(ErrorCode.FILE_NOT_FOUND);
      }
      try {
        boolean success = ImageUtils.createThumb(orginFile, thumbFile, context.getThumbnail());
        if (!success) {
          param.throwException(ErrorCode.FILE_NOT_FOUND);
        }
      } catch (IOException | ConnectorException e) {
        try {
          Files.deleteIfExists(thumbFile);
        } catch (IOException ex) {
          e.addSuppressed(ex);
        }
        throw new ConnectorException(ErrorCode.ACCESS_DENIED, e);
      }
    }

    response.setHeader("Cache-Control", "public");

    try {
      PartialHolder.INSTANCE.service(request, response, thumbFile);
    } catch (UncheckedConnectorException ex) {
      throw ex.getCause();
    } catch (ServletException ex) {
      throw new AssertionError(ex);
    } catch (IOException ex) {
      throw new ConnectorException(ErrorCode.ACCESS_DENIED, ex);
    }
  }

  @Override
  protected ThumbnailParameter popupParams(HttpServletRequest request, CKFinderContext context)
          throws ConnectorException {
    ThumbnailParameter param = doInitParam(new ThumbnailParameter(), request, context);
    param.setFileName(request.getParameter("FileName"));
    return param;
  }

  @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
  private static class PartialHolder {

    static PathPartial INSTANCE;

    static {
      INSTANCE = PathPartial.builder()
              .notFound(context -> {
                throw new UncheckedConnectorException(ErrorCode.FILE_NOT_FOUND);
              })
              .contentDisposition(ContentDisposition.inline())
              .build();
    }

  }

}
