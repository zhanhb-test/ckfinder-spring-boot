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
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ImageInfoElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class ImageResizeInfoCommand extends FinishOnErrorXmlCommand<String> {

  @Override
  protected void createXml(@Nullable String fileName, CommandContext cmdContext, ConnectorElement.Builder rootElement) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();

    cmdContext.checkAllPermission(AccessControl.FILE_VIEW);

    if (!StringUtils.hasLength(fileName)
            || FileUtils.isFileNameInvalid(fileName)
            || context.isFileHidden(fileName)) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }

    Path imageFile = cmdContext.resolve(fileName);

    if (!Files.isRegularFile(imageFile)) {
      throw cmdContext.toException(ErrorCode.FILE_NOT_FOUND);
    }

    try {
      BufferedImage image;
      try (InputStream is = Files.newInputStream(imageFile)) {
        image = ImageIO.read(is);
      }
      rootElement.result(ImageInfoElement.builder()
              .width(image.getWidth())
              .height(image.getHeight()).build());
    } catch (IOException e) {
      log.error("failed to access file '{}'", imageFile, e);
      throw cmdContext.toException(ErrorCode.ACCESS_DENIED).initCause(e);
    }
  }

  @Override
  protected String parseParameters(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("fileName");
  }

}
