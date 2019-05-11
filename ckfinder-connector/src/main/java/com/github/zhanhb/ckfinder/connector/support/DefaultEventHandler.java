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
package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.EventHandler;
import com.github.zhanhb.ckfinder.connector.api.FileUploadEvent;
import com.github.zhanhb.ckfinder.connector.api.FileUploadListener;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides support for event handlers.
 */
@Slf4j
class DefaultEventHandler implements EventHandler {

  private final List<FileUploadListener> fileUploadListeners;

  DefaultEventHandler(List<FileUploadListener> fileUploadListeners) {
    this.fileUploadListeners = fileUploadListeners;
  }

  @Override
  public void fireOnFileUpload(FileUploadEvent event) {
    log.trace("{}", fileUploadListeners);
    for (FileUploadListener listener : fileUploadListeners) {
      listener.onFileUploadComplete(event);
    }
  }

}
