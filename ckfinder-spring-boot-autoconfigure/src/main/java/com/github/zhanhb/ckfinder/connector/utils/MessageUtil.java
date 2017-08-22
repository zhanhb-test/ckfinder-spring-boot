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
package com.github.zhanhb.ckfinder.connector.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Error utils.
 */
public enum MessageUtil {

  INSTANCE;

  private static final String BUNDLE_NAME = MessageUtil.class.getPackage()
          .getName().concat(".LocalStrings");

  /**
   * Gets error message by locale code.
   *
   * @param errorCode error number
   * @param lang connector language code
   * @return localized error message.
   */
  public String getMessage(String lang, int errorCode) {
    try {
      return ResourceBundle.getBundle(BUNDLE_NAME, Locale.forLanguageTag(lang)).getString(Integer.toString(errorCode));
    } catch (RuntimeException ex) {
      return "";
    }
  }

}
