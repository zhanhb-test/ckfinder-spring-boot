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

import com.github.zhanhb.ckfinder.connector.api.InitPluginInfo;
import com.github.zhanhb.ckfinder.connector.api.PluginInfoRegister;
import com.github.zhanhb.ckfinder.connector.handlers.response.ImageResizeInfo;
import java.util.Map;
import javax.xml.namespace.QName;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageResizeRegister implements PluginInfoRegister {

  private final ImageResizeInfo imageResizeInfo;

  @SuppressWarnings("WeakerAccess")
  public ImageResizeRegister(Map<ImageResizeParam, ImageResizeSize> params){
    ImageResizeInfo.Builder builder = ImageResizeInfo.builder();
    for (Map.Entry<ImageResizeParam, ImageResizeSize> entry : params.entrySet()) {
      QName key = QName.valueOf(entry.getKey().getXmlKey());
      String value = entry.getValue().toString();
      builder.attribute(key, value);
    }
    imageResizeInfo = builder.build();
  }

  @Override
  public void addPluginDataTo(InitPluginInfo info) {
    log.debug("addPluginDataTo: {}", info);
    info.add(imageResizeInfo);
  }

}
