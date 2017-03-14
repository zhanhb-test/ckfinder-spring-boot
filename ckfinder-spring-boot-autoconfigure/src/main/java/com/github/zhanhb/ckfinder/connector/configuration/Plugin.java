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
package com.github.zhanhb.ckfinder.connector.configuration;

import java.util.Set;

/**
 * Base class for plugins.
 */
public abstract class Plugin {

  protected abstract void registerPluginName(Set<String> names);

  /**
   * register event handlers for plugin.
   *
   * @param eventHandler available event handlers.
   */
  protected abstract void registerEventHandlers(Events.Builder eventHandler);

  protected abstract void registerCommands(CommandFactoryBuilder factory);

}
