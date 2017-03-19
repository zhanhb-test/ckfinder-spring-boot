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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizeCommand extends BaseXmlCommand<ImageResizeParameter> implements IPostCommand {

  private static final String[] SIZES = {"small", "medium", "large"};

  private final Map<String, String> pluginParams;

  public ImageResizeCommand(Map<String, String> params) {
    super(ImageResizeParameter::new);
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

    if (param.getFileName() == null || param.getFileName().isEmpty()) {
      param.throwException(ConnectorError.INVALID_NAME);
    }

    if (!FileUtils.isFileNameValid(param.getFileName())
            || FileUtils.isFileHidden(param.getFileName(), configuration)) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllowed(param.getFileName(), param.getType())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    Path file = Paths.get(param.getType().getPath(),
            param.getCurrentFolder(),
            param.getFileName());
    try {
      if (!Files.isRegularFile(file)) {
        param.throwException(ConnectorError.FILE_NOT_FOUND);
      }

      if (param.isWrongReqSizesParams()) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (param.getWidth() != null && param.getHeight() != null) {

        if (!FileUtils.isFileNameValid(param.getNewFileName())
                && FileUtils.isFileHidden(param.getNewFileName(), configuration)) {
          param.throwException(ConnectorError.INVALID_NAME);
        }

        if (!FileUtils.isFileExtensionAllowed(param.getNewFileName(),
                param.getType())) {
          param.throwException(ConnectorError.INVALID_EXTENSION);
        }

        Path thumbFile = Paths.get(param.getType().getPath(),
                param.getCurrentFolder(),
                param.getNewFileName());

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

      String fileNameWithoutExt = FileUtils.getFileNameWithoutExtension(param.getFileName());
      String fileExt = FileUtils.getFileExtension(param.getFileName());
      for (String size : SIZES) {
        if ("1".equals(param.getSizesFromReq().get(size))) {
          String thumbName = fileNameWithoutExt + "_" + size + "." + fileExt;
          Path thumbFile = Paths.get(param.getType().getPath(),
                  param.getCurrentFolder(), thumbName);
          String value = pluginParams.get(size + "Thumb");
          if (value != null) {
            Matcher matcher = Pattern.compile("(\\d+)x(\\d+)").matcher(value);
            if (matcher.matches()) {
              String[] params = new String[]{matcher.group(1), matcher.group(2)};
              try {
                ImageUtils.createResizedImage(file, thumbFile, Integer.parseInt(params[0]),
                        Integer.parseInt(params[1]), configuration.getImgQuality());
              } catch (IOException e) {
                log.error("", e);
                param.throwException(ConnectorError.ACCESS_DENIED);
              }
            }
          }
        }
      }
    } catch (SecurityException e) {
      log.error("", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected void initParams(ImageResizeParameter param, HttpServletRequest request,
          IConfiguration configuration) throws ConnectorException {
    super.initParams(param, request, configuration);

    param.setSizesFromReq(new HashMap<>());
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    param.setOverwrite(request.getParameter("overwrite"));
    String reqWidth = request.getParameter("width");
    String reqHeight = request.getParameter("height");
    param.setWrongReqSizesParams(false);
    try {
      if (reqWidth != null && !reqWidth.isEmpty()) {
        param.setWidth(Integer.valueOf(reqWidth));
      } else {
        param.setWidth(null);
      }
    } catch (NumberFormatException e) {
      param.setWidth(null);
      param.setWrongReqSizesParams(true);
    }
    try {
      if (reqHeight != null && !reqHeight.isEmpty()) {
        param.setHeight(Integer.valueOf(reqHeight));
      } else {
        param.setHeight(null);
      }
    } catch (NumberFormatException e) {
      param.setHeight(null);
      param.setWrongReqSizesParams(true);
    }
    for (String size : SIZES) {
      param.getSizesFromReq().put(size, request.getParameter(size));
    }
    param.setRequest(request);
  }

}
