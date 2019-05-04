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
import com.github.zhanhb.ckfinder.connector.api.ImageProperties;
import com.github.zhanhb.ckfinder.connector.handlers.command.BaseXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ImageResizeParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizeCommand extends BaseXmlCommand<ImageResizeParameter> implements IPostCommand {

  private final Map<ImageResizeParam, ImageResizeSize> pluginParams;

  ImageResizeCommand(Map<ImageResizeParam, ImageResizeSize> params) {
    this.pluginParams = params;
  }

  @Override
  protected void createXml(Connector.Builder rootElement, ImageResizeParameter param, CommandContext cmdContext) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_DELETE | AccessControl.FILE_UPLOAD);

    String fileName = param.getFileName();
    String newFileName = param.getNewFileName();

    if (fileName == null || fileName.isEmpty()) {
      cmdContext.throwException(ErrorCode.INVALID_NAME);
    }

    if (!FileUtils.isFileNameValid(fileName) || context.isFileHidden(fileName)) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    Path file = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder(), fileName);
    if (!Files.isRegularFile(file)) {
      cmdContext.throwException(ErrorCode.FILE_NOT_FOUND);
    }

    if (param.isWrongReqSizesParams()) {
      cmdContext.throwException(ErrorCode.INVALID_REQUEST);
    }

    ImageProperties image = context.getImage();
    if (param.getWidth() != null && param.getHeight() != null) {

      if (!FileUtils.isFileNameValid(newFileName) && context.isFileHidden(newFileName)) {
        cmdContext.throwException(ErrorCode.INVALID_NAME);
      }

      if (!FileUtils.isFileExtensionAllowed(newFileName, cmdContext.getType())) {
        cmdContext.throwException(ErrorCode.INVALID_EXTENSION);
      }

      Path thumbFile = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder(), newFileName);

      if (Files.exists(thumbFile) && !Files.isWritable(thumbFile)) {
        cmdContext.throwException(ErrorCode.ACCESS_DENIED);
      }
      if (!"1".equals(param.getOverwrite()) && Files.exists(thumbFile)) {
        cmdContext.throwException(ErrorCode.ALREADY_EXIST);
      }
      int maxImageHeight = image.getMaxHeight();
      int maxImageWidth = image.getMaxWidth();
      if ((maxImageWidth > 0 && param.getWidth() > maxImageWidth)
              || (maxImageHeight > 0 && param.getHeight() > maxImageHeight)) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      try {
        ImageUtils.createResizedImage(file, thumbFile,
                param.getWidth(), param.getHeight(), image.getQuality());

      } catch (IOException e) {
        log.error("", e);
        cmdContext.throwException(ErrorCode.ACCESS_DENIED);
      }
    }

    String fileNameWithoutExt = FileUtils.getNameWithoutExtension(fileName);
    String fileExt = FileUtils.getExtension(fileName);
    for (ImageResizeParam key : ImageResizeParam.values()) {
      if ("1".equals(param.getSizesFromReq().get(key))) {
        String thumbName = fileNameWithoutExt + "_" + key.getParameter() + "." + fileExt;
        Path thumbFile = getPath(cmdContext.getType().getPath(), cmdContext.getCurrentFolder(), thumbName);
        ImageResizeSize size = pluginParams.get(key);
        if (size != null) {
          try {
            ImageUtils.createResizedImage(file, thumbFile, size.getWidth(),
                    size.getHeight(), image.getQuality());
          } catch (IOException e) {
            log.error("", e);
            cmdContext.throwException(ErrorCode.ACCESS_DENIED);
          }
        }
      }
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected ImageResizeParameter popupParams(HttpServletRequest request, CKFinderContext context) throws ConnectorException {
    ImageResizeParameter param = new ImageResizeParameter();
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
