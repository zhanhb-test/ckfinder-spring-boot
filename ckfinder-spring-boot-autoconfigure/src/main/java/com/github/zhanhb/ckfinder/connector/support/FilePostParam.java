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

import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Builder;
import lombok.Value;

/**
 * File from param entity.
 */
@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@Value
public class FilePostParam {

  private String folder;
  private String name;
  private String options;
  private ResourceType type;

  public Path toPath() {
    return type.resolve(folder, name);
  }

  public Optional<Path> toThumbnailPath() {
    return type.resolveThumbnail(folder, name);
  }

}
