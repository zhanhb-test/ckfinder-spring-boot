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

/**
 * Class holding constants used by the CKFinder connector.
 */
public interface Constants {

  int DEFAULT_IMG_WIDTH = 500;
  int DEFAULT_IMG_HEIGHT = 400;
  int DEFAULT_THUMB_MAX_WIDTH = 100;
  int DEFAULT_THUMB_MAX_HEIGHT = 100;
  float DEFAULT_IMG_QUALITY = 0.8f;
  String DEFAULT_THUMBS_URL = "_thumbs";
  String DEFAULT_THUMBS_DIR = "_thumbs";
  String DEFAULT_BASE_URL = "/userfiles";

  /**
   * Regular expression to identify invalid characters in file name.
   */
  String INVALID_FILE_NAME_REGEX = "\\p{Cntrl}|[/\\\\\\:\\*\\?\"\\<\\>\\|]";

  /**
   * Regular expression to identify invalid characters in path.
   */
  String INVALID_PATH_REGEX = "(/\\.|\\p{Cntrl}|//|\\\\|[:*?<>\"\\|])";

  /**
   * Regular expression to identify full URL.
   */
  String URL_REGEX = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$";

}
