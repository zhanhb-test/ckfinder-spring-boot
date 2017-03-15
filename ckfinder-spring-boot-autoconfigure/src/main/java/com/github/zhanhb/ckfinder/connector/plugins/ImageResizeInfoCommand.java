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
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ImageResizeInfoParameter;
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
public class ImageResizeInfoCommand extends BaseXmlCommand<ImageResizeInfoParameter> {

  public ImageResizeInfoCommand() {
    super(ImageResizeInfoParameter::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, ImageResizeInfoParameter param, IConfiguration configuration) {
    createImageInfoNode(rootElement, param);
  }

  private void createImageInfoNode(Connector.Builder rootElement, ImageResizeInfoParameter param) {
    ImageInfo.Builder element = ImageInfo.builder();
    element.width(param.getImageWidth())
            .height(param.getImageHeight());
    rootElement.imageInfo(element.build());
  }

  @Override
  protected void createXml(ImageResizeInfoParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (param.getFileName() == null || param.getFileName().isEmpty()
            || !FileUtils.isFileNameInvalid(param.getFileName())
            || FileUtils.isFileHidden(param.getFileName(), configuration)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllwed(param.getFileName(), param.getType())) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path imageFile = Paths.get(param.getType().getPath(),
            param.getCurrentFolder(),
            param.getFileName());

    try {
      if (!Files.isRegularFile(imageFile)) {
        param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }

      BufferedImage image;
      try (InputStream is = Files.newInputStream(imageFile)) {
        image = ImageIO.read(is);
      }
      param.setImageWidth(image.getWidth());
      param.setImageHeight(image.getHeight());
    } catch (SecurityException | IOException e) {
      log.error("", e);
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
  }

  @Override
  protected void initParams(ImageResizeInfoParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setImageHeight(0);
    param.setImageWidth(0);
    param.setCurrentFolder(request.getParameter("currentFolder"));
    param.setType(configuration.getTypes().get(request.getParameter("type")));
    param.setFileName(request.getParameter("fileName"));
  }

}
