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
import com.github.zhanhb.ckfinder.connector.handlers.command.SuccessXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.ImageInfo;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

@Slf4j
public class ImageResizeInfoCommand extends SuccessXmlCommand<String> {

  @Override
  protected void createXml(Connector.Builder rootElement, String fileName, CommandContext cmdContext) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();

    cmdContext.checkAllPermission(AccessControl.FILE_VIEW);

    if (StringUtils.isEmpty(fileName)
            || !FileUtils.isFileNameValid(fileName)
            || context.isFileHidden(fileName)) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path imageFile = cmdContext.resolve(fileName);

    if (!Files.isRegularFile(imageFile)) {
      cmdContext.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    try {
      BufferedImage image;
      try (InputStream is = Files.newInputStream(imageFile)) {
        image = ImageIO.read(is);
      }
      rootElement.result(ImageInfo.builder()
              .width(image.getWidth())
              .height(image.getHeight()).build());
    } catch (IOException e) {
      log.error("failed to access file '{}'", imageFile, e);
      cmdContext.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  @Override
  protected String popupParams(HttpServletRequest request, CKFinderContext context) {
    return request.getParameter("fileName");
  }

}
