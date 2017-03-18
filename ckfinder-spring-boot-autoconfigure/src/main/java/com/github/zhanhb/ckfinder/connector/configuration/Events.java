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

import com.github.zhanhb.ckfinder.connector.data.FileUploadEvent;
import com.github.zhanhb.ckfinder.connector.data.FileUploadListener;
import com.github.zhanhb.ckfinder.connector.data.InitCommandEvent;
import com.github.zhanhb.ckfinder.connector.data.PluginInfoRegister;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides support for event handlers.
 */
@Slf4j
public class Events {

  private final List<FileUploadListener> fileUploadListeners;
  private final List<PluginInfoRegister> pluginInfoRegisters;

  Events(List<FileUploadListener> fileUploadListeners, List<PluginInfoRegister> pluginInfoRegisters) {
    this.fileUploadListeners = fileUploadListeners;
    this.pluginInfoRegisters = pluginInfoRegisters;
  }

  @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
  public void fireOnFileUpload(FileUploadEvent args, IConfiguration configuration)
          throws ConnectorException {
    log.trace("{}", fileUploadListeners);
    try {
      for (FileUploadListener eventHandler : fileUploadListeners) {
        eventHandler.onFileUploadComplete(args, configuration);
      }
    } catch (ConnectorException ex) {
      throw ex;
    } catch (Exception e) {
      throw new ConnectorException(e);
    }
  }

  public void runInitCommand(InitCommandEvent event, IConfiguration configuration) {
    log.trace("{}", pluginInfoRegisters);
    for (PluginInfoRegister pluginInfoRegister : pluginInfoRegisters) {
      pluginInfoRegister.runEventHandler(event, configuration);
    }
  }

}
