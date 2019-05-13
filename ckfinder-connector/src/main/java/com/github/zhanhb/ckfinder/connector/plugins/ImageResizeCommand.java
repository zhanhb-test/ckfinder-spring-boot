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
import com.github.zhanhb.ckfinder.connector.handlers.command.FinishOnErrorXmlCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ImageResizeParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
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
public class ImageResizeCommand extends FinishOnErrorXmlCommand<ImageResizeParameter> implements IPostCommand {

  private final Map<ImageResizeParam, ImageResizeSize> pluginParams;

  ImageResizeCommand(Map<ImageResizeParam, ImageResizeSize> params) {
    this.pluginParams = params;
  }

  @Override
  protected void createXml(ImageResizeParameter param, CommandContext cmdContext, ConnectorElement.Builder builder) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_DELETE | AccessControl.FILE_UPLOAD);

    String fileName = param.getFileName();
    String newFileName = param.getNewFileName();

    if (fileName == null || fileName.isEmpty()) {
      throw cmdContext.toException(ErrorCode.INVALID_NAME);
    }

    if (FileUtils.isFileNameInvalid(fileName) || context.isFileHidden(fileName)) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }

    String[] nameAndExtension = FileUtils.getNameAndExtension(fileName);
    if (nameAndExtension == null) {
      throw cmdContext.toException(ErrorCode.INVALID_NAME);
    }

    if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }

    if (param.isWrongReqSizesParams()) {
      throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
    }

    Path file = cmdContext.resolve(fileName);
    if (!Files.isRegularFile(file)) {
      throw cmdContext.toException(ErrorCode.FILE_NOT_FOUND);
    }

    ImageProperties image = context.getImage();
    Integer width = param.getWidth();
    Integer height = param.getHeight();
    if (width != null && height != null) {

      if (FileUtils.isFileNameInvalid(newFileName) && context.isFileHidden(newFileName)) {
        throw cmdContext.toException(ErrorCode.INVALID_NAME);
      }

      if (!FileUtils.isFileExtensionAllowed(newFileName, cmdContext.getType())) {
        throw cmdContext.toException(ErrorCode.INVALID_EXTENSION);
      }

      Path target = cmdContext.resolve(newFileName);

      if (Files.exists(target) && !Files.isWritable(target)) {
        throw cmdContext.toException(ErrorCode.ACCESS_DENIED);
      }
      if (!"1".equals(param.getOverwrite()) && Files.exists(target)) {
        throw cmdContext.toException(ErrorCode.ALREADY_EXIST);
      }
      int maxImageHeight = image.getMaxHeight();
      int maxImageWidth = image.getMaxWidth();
      if ((maxImageWidth > 0 && width > maxImageWidth)
              || (maxImageHeight > 0 && height > maxImageHeight)) {
        throw cmdContext.toException(ErrorCode.INVALID_REQUEST);
      }

      try {
        ImageUtils.createResizedImage(file, target,
                width, height, image.getQuality());
      } catch (IOException e) {
        log.error("", e);
        throw cmdContext.toException(ErrorCode.ACCESS_DENIED).initCause(e);
      }
    }

    for (Map.Entry<ImageResizeParam, String> entry : param.getSizes().entrySet()) {
      ImageResizeParam key = entry.getKey();
      if ("1".equals(entry.getValue())) {
        String thumbName = nameAndExtension[0] + "_" + key.getParameter() + "." + nameAndExtension[1];
        Path thumbFile = cmdContext.resolve(thumbName);
        ImageResizeSize size = pluginParams.get(key);
        if (size != null) {
          try {
            ImageUtils.createResizedImage(file, thumbFile, size.getWidth(),
                    size.getHeight(), image.getQuality());
          } catch (IOException e) {
            log.error("", e);
            throw cmdContext.toException(ErrorCode.ACCESS_DENIED).initCause(e);
          }
        }
      }
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected ImageResizeParameter parseParameters(HttpServletRequest request, CKFinderContext context) {
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
    Map<ImageResizeParam, String> requestSizes = param.getSizes();
    for (ImageResizeParam size : ImageResizeParam.values()) {
      requestSizes.put(size, request.getParameter(size.getParameter()));
    }
    return param;
  }

}
