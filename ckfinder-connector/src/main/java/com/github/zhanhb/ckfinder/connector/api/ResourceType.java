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

import java.nio.file.Path;
import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * Resource type interface.
 */
public interface ResourceType {

  /**
   * resource name.
   */
  String getName();

  /**
   * resource url.
   */
  String getUrl();

  /**
   * resource directory.
   */
  @Nonnull
  Path getPath();

  /**
   * list of allowed extensions in resource (separated with comma).
   */
  String getAllowedExtensions();

  /**
   * list of denied extensions in resource (separated with comma).
   */
  String getDeniedExtensions();

  /**
   * max file size in resource.
   */
  long getMaxSize();

  /**
   * check if file size isn't bigger then max size for type.
   *
   * @param fileSize file size
   * @return true if file size isn't bigger then max size for type.
   */
  default boolean isFileSizeOutOfRange(long fileSize) {
    final long maxSize = getMaxSize();
    return maxSize != 0 && fileSize > maxSize;
  }

  Path resolve(String... names);

  Optional<Path> resolveThumbnail(String... names);

}
