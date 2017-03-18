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
package com.github.zhanhb.ckfinder.connector.data;

import com.github.zhanhb.ckfinder.connector.configuration.PluginRegister;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;

/**
 * Event data for {@link PluginRegister#addPluginInfoRegister} event.
 */
public class InitCommandEvent {

  private final Connector.Builder connector;

  public InitCommandEvent(Connector.Builder connector) {
    this.connector = connector;
  }

  public Connector.Builder getConnector() {
    return connector;
  }

}
