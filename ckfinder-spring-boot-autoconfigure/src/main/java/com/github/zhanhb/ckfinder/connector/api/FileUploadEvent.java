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

import com.github.zhanhb.ckfinder.connector.support.PluginRegister;
import java.nio.file.Path;

/**
 * Event data for {@link PluginRegister#addFileUploadListener} event.
 */
public class FileUploadEvent {

  private final String currentFolder;
  private final Path file;

  public FileUploadEvent(String currentFolder, Path path) {
    this.currentFolder = currentFolder;
    this.file = path;
  }

  public String getCurrentFolder() {
    return currentFolder;
  }

  public Path getFile() {
    return file;
  }

}
