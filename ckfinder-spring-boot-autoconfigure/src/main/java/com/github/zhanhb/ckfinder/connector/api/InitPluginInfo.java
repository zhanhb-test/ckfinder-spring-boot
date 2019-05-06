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
package com.github.zhanhb.ckfinder.connector.api;

import com.github.zhanhb.ckfinder.connector.handlers.response.PluginInfo;
import com.github.zhanhb.ckfinder.connector.handlers.response.PluginsInfos;

/**
 * Event data for {@link PluginRegistry#addPluginInfoRegister} event.
 */
public class InitPluginInfo {

  private PluginsInfos.Builder builder;

  public void add(PluginInfo pluginInfo) {
    PluginsInfos.Builder b = builder;
    if (b == null) {
      b = PluginsInfos.builder();
      builder = b;
    }
    b.pluginsInfo(pluginInfo);
  }

  public PluginsInfos build() {
    PluginsInfos.Builder b = builder;
    return b != null ? b.build() : null;
  }

}
