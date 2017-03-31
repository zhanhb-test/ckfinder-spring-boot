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

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ImageResizeParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizeCommand extends BaseXmlCommand<ImageResizeParameter> implements IPostCommand {

  private final Map<ImageResizeParam, ImageResizeSize> pluginParams;

  public ImageResizeCommand(Map<ImageResizeParam, ImageResizeSize> params) {
    this.pluginParams = params;
  }

  @Override
  protected void createXml(Connector.Builder rootElement, ImageResizeParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    String fileName = param.getFileName();
    String newFileName = param.getNewFileName();

    if (fileName == null || fileName.isEmpty()) {
      param.throwException(ConnectorError.INVALID_NAME);
    }

    if (!FileUtils.isFileNameValid(fileName) || configuration.isFileHidden(fileName)) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, param.getType())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    Path file = Paths.get(param.getType().getPath(), param.getCurrentFolder(), fileName);
    if (!Files.isRegularFile(file)) {
      param.throwException(ConnectorError.FILE_NOT_FOUND);
    }

    if (param.isWrongReqSizesParams()) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (param.getWidth() != null && param.getHeight() != null) {

      if (!FileUtils.isFileNameValid(newFileName) && configuration.isFileHidden(newFileName)) {
        param.throwException(ConnectorError.INVALID_NAME);
      }

      if (!FileUtils.isFileExtensionAllowed(newFileName, param.getType())) {
        param.throwException(ConnectorError.INVALID_EXTENSION);
      }

      Path thumbFile = Paths.get(param.getType().getPath(), param.getCurrentFolder(), newFileName);

      if (Files.exists(thumbFile) && !Files.isWritable(thumbFile)) {
        param.throwException(ConnectorError.ACCESS_DENIED);
      }
      if (!"1".equals(param.getOverwrite()) && Files.exists(thumbFile)) {
        param.throwException(ConnectorError.ALREADY_EXIST);
      }
      int maxImageHeight = configuration.getImgHeight();
      int maxImageWidth = configuration.getImgWidth();
      if ((maxImageWidth > 0 && param.getWidth() > maxImageWidth)
              || (maxImageHeight > 0 && param.getHeight() > maxImageHeight)) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      try {
        ImageUtils.createResizedImage(file, thumbFile,
                param.getWidth(), param.getHeight(), configuration.getImgQuality());

      } catch (IOException e) {
        log.error("", e);
        param.throwException(ConnectorError.ACCESS_DENIED);
      }
    }

    String fileNameWithoutExt = FileUtils.getFileNameWithoutExtension(fileName);
    String fileExt = FileUtils.getFileExtension(fileName);
    for (ImageResizeParam key : ImageResizeParam.values()) {
      if ("1".equals(param.getSizesFromReq().get(key))) {
        String thumbName = fileNameWithoutExt + "_" + key.getParameter() + "." + fileExt;
        Path thumbFile = Paths.get(param.getType().getPath(), param.getCurrentFolder(), thumbName);
        ImageResizeSize size = pluginParams.get(key);
        if (size != null) {
          try {
            ImageUtils.createResizedImage(file, thumbFile, size.getWidth(),
                    size.getHeight(), configuration.getImgQuality());
          } catch (IOException e) {
            log.error("", e);
            param.throwException(ConnectorError.ACCESS_DENIED);
          }
        }
      }
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected ImageResizeParameter popupParams(HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    ImageResizeParameter param = doInitParam(new ImageResizeParameter(), request, configuration);
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    param.setOverwrite(request.getParameter("overwrite"));
    String reqWidth = request.getParameter("width");
    String reqHeight = request.getParameter("height");
    try {
      if (reqWidth != null && !reqWidth.isEmpty()) {
        param.setWidth(Integer.valueOf(reqWidth));
      } else {
        param.setWidth(null);
      }
      if (reqHeight != null && !reqHeight.isEmpty()) {
        param.setHeight(Integer.valueOf(reqHeight));
      } else {
        param.setHeight(null);
      }
    } catch (NumberFormatException e) {
      param.setWidth(null);
      param.setHeight(null);
      param.setWrongReqSizesParams(true);
    }
    for (ImageResizeParam size : ImageResizeParam.values()) {
      param.getSizesFromReq().put(size, request.getParameter(size.getParameter()));
    }
    return param;
  }

}
