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

import com.github.zhanhb.ckfinder.connector.configuration.CommandFactoryBuilder;
import com.github.zhanhb.ckfinder.connector.configuration.Events;
import com.github.zhanhb.ckfinder.connector.configuration.Plugin;
import java.util.Set;

public class FileEditor extends Plugin {

  @Override
  protected void registerPluginName(Set<String> names) {
    names.add("fileeditor");
  }

  @Override
  public void registerEventHandlers(Events.Builder events) {
  }

  @Override
  protected void registerCommands(CommandFactoryBuilder factory) {
    factory.registerCommands(new SaveFileCommand());
  }

}
