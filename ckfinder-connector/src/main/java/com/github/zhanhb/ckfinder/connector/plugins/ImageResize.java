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

import com.github.zhanhb.ckfinder.connector.api.Plugin;
import com.github.zhanhb.ckfinder.connector.api.PluginRegistry;
import java.util.Map;
import javax.xml.namespace.QName;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ImageResize implements Plugin {

  private final Map<ImageResizeParam, ImageResizeSize> params;

  @Override
  public void register(PluginRegistry registry) {
    String name = "imageresize";
    ImageResizeInfo.Builder builder = ImageResizeInfo.builder().name(name);
    for (Map.Entry<ImageResizeParam, ImageResizeSize> entry : params.entrySet()) {
      QName key = QName.valueOf(entry.getKey().getXmlKey());
      String value = entry.getValue().toString();
      builder.attribute(key, value);
    }
    registry.addName(name)
            .addPluginInfo(builder.build())
            .registerCommands(new ImageResizeCommand(params),
                    new ImageResizeInfoCommand());
  }

}
