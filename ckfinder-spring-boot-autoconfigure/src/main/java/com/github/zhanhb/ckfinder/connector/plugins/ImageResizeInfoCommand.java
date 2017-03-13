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

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.ImageResizeInfoArguments;
import com.github.zhanhb.ckfinder.connector.handlers.command.XMLCommand;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.ImageInfo;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizeInfoCommand extends XMLCommand<ImageResizeInfoArguments> {

  public ImageResizeInfoCommand() {
    super(ImageResizeInfoArguments::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, ImageResizeInfoArguments arguments, IConfiguration configuration) {
    createImageInfoNode(rootElement, arguments);
  }

  private void createImageInfoNode(Connector.Builder rootElement, ImageResizeInfoArguments arguments) {
    ImageInfo.Builder element = ImageInfo.builder();
    element.width(arguments.getImageWidth())
            .height(arguments.getImageHeight());
    rootElement.imageInfo(element.build());
  }

  @Override
  protected void createXml(ImageResizeInfoArguments arguments, IConfiguration configuration) throws ConnectorException {
    if (arguments.getType() == null) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (arguments.getFileName() == null || arguments.getFileName().isEmpty()
            || !FileUtils.isFileNameInvalid(arguments.getFileName())
            || FileUtils.isFileHidden(arguments.getFileName(), configuration)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllwed(arguments.getFileName(), arguments.getType())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path imageFile = Paths.get(arguments.getType().getPath(),
            arguments.getCurrentFolder(),
            arguments.getFileName());

    try {
      if (!Files.isRegularFile(imageFile)) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }

      BufferedImage image;
      try (InputStream is = Files.newInputStream(imageFile)) {
        image = ImageIO.read(is);
      }
      arguments.setImageWidth(image.getWidth());
      arguments.setImageHeight(image.getHeight());
    } catch (SecurityException | IOException e) {
      log.error("", e);
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
  }

  @Override
  protected void initParams(ImageResizeInfoArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);
    arguments.setImageHeight(0);
    arguments.setImageWidth(0);
    arguments.setCurrentFolder(request.getParameter("currentFolder"));
    arguments.setType(configuration.getTypes().get(request.getParameter("type")));
    arguments.setFileName(request.getParameter("fileName"));
  }

}
