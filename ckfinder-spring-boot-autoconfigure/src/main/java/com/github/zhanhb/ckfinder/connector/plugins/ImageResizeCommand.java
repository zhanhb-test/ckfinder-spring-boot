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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.ImageResizeArguments;
import com.github.zhanhb.ckfinder.connector.handlers.command.XMLCommand;
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
public class ImageResizeCommand extends XMLCommand<ImageResizeArguments> {

  private static final String[] SIZES = {"small", "medium", "large"};

  private final Map<String, String> pluginParams;

  public ImageResizeCommand(Map<String, String> params) {
    super(ImageResizeArguments::new);
    this.pluginParams = params;
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, ImageResizeArguments arguments, IConfiguration configuration) {
  }

  @Override
  protected void createXml(ImageResizeArguments arguments, IConfiguration configuration) throws ConnectorException {
    if (arguments.getType() == null) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(arguments.getType().getName(),
            arguments.getCurrentFolder(), arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE
            | AccessControl.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    if (arguments.getFileName() == null || arguments.getFileName().isEmpty()) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
    }

    if (!FileUtils.isFileNameInvalid(arguments.getFileName())
            || FileUtils.isFileHidden(arguments.getFileName(), configuration)) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    if (!FileUtils.isFileExtensionAllwed(arguments.getFileName(), arguments.getType())) {
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path file = Paths.get(arguments.getType().getPath(),
            arguments.getCurrentFolder(),
            arguments.getFileName());
    try {
      if (!Files.isRegularFile(file)) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND);
      }

      if (arguments.isWrongReqSizesParams()) {
        arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
      }

      if (arguments.getWidth() != null && arguments.getHeight() != null) {

        if (!FileUtils.isFileNameInvalid(arguments.getNewFileName())
                && FileUtils.isFileHidden(arguments.getNewFileName(), configuration)) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
        }

        if (!FileUtils.isFileExtensionAllwed(arguments.getNewFileName(),
                arguments.getType())) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION);
        }

        Path thumbFile = Paths.get(arguments.getType().getPath(),
                arguments.getCurrentFolder(),
                arguments.getNewFileName());

        if (Files.exists(thumbFile) && !Files.isWritable(thumbFile)) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
        }
        if (!"1".equals(arguments.getOverwrite()) && Files.exists(thumbFile)) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST);
        }
        int maxImageHeight = configuration.getImgHeight();
        int maxImageWidth = configuration.getImgWidth();
        if ((maxImageWidth > 0 && arguments.getWidth() > maxImageWidth)
                || (maxImageHeight > 0 && arguments.getHeight() > maxImageHeight)) {
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
        }

        try {
          ImageUtils.createResizedImage(file, thumbFile,
                  arguments.getWidth(), arguments.getHeight(), configuration.getImgQuality());

        } catch (IOException e) {
          log.error("", e);
          arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
        }
      }

      String fileNameWithoutExt = FileUtils.getFileNameWithoutExtension(arguments.getFileName());
      String fileExt = FileUtils.getFileExtension(arguments.getFileName());
      for (String size : SIZES) {
        if ("1".equals(arguments.getSizesFromReq().get(size))) {
          String thumbName = fileNameWithoutExt + "_" + size + "." + fileExt;
          Path thumbFile = Paths.get(arguments.getType().getPath(),
                  arguments.getCurrentFolder(), thumbName);
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
                arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
              }
            }
          }
        }
      }
    } catch (SecurityException e) {
      log.error("", e);
      arguments.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected void initParams(ImageResizeArguments arguments, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(arguments, request, configuration);

    arguments.setSizesFromReq(new HashMap<>());
    arguments.setFileName(request.getParameter("fileName"));
    arguments.setNewFileName(request.getParameter("newFileName"));
    arguments.setOverwrite(request.getParameter("overwrite"));
    String reqWidth = request.getParameter("width");
    String reqHeight = request.getParameter("height");
    arguments.setWrongReqSizesParams(false);
    try {
      if (reqWidth != null && !reqWidth.isEmpty()) {
        arguments.setWidth(Integer.valueOf(reqWidth));
      } else {
        arguments.setWidth(null);
      }
    } catch (NumberFormatException e) {
      arguments.setWidth(null);
      arguments.setWrongReqSizesParams(true);
    }
    try {
      if (reqHeight != null && !reqHeight.isEmpty()) {
        arguments.setHeight(Integer.valueOf(reqHeight));
      } else {
        arguments.setHeight(null);
      }
    } catch (NumberFormatException e) {
      arguments.setHeight(null);
      arguments.setWrongReqSizesParams(true);
    }
    for (String size : SIZES) {
      arguments.getSizesFromReq().put(size, request.getParameter(size));
    }
    arguments.setRequest(request);
  }

}
