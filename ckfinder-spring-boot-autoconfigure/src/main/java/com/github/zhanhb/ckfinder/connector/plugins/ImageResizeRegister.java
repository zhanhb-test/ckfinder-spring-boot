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

import com.github.zhanhb.ckfinder.connector.api.InitCommandEvent;
import com.github.zhanhb.ckfinder.connector.api.PluginInfoRegister;
import com.github.zhanhb.ckfinder.connector.handlers.response.ImageResizeInfo;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ImageResizeRegister implements PluginInfoRegister {

  private final Map<ImageResizeParam, ImageResizeSize> params;

  @Override
  public void onInitEvent(InitCommandEvent event) {
    log.debug("runEventHandler: {}", event);
    ImageResizeInfo.Builder builder = ImageResizeInfo.builder();
    for (Map.Entry<ImageResizeParam, ImageResizeSize> entry : params.entrySet()) {
      String key = entry.getKey().getXmlKey();
      String value = entry.getValue().toString();
      builder.attr(key, value);
    }
    event.getBuilder().pluginsInfo(builder.build());
  }

}
