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
import java.util.Optional;
import java.util.ResourceBundle;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

/**
 * Error utils.
 */
@Slf4j
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
  public String getMessage(@Nullable String lang, int errorCode) {
    Locale locale = Optional.ofNullable(lang).map(Locale::forLanguageTag).orElse(Locale.ROOT);
    try {
      return ResourceBundle.getBundle(BUNDLE_NAME, locale).getString(Integer.toString(errorCode));
    } catch (RuntimeException ex) {
      log.debug("",ex);
      return "";
    }
  }

}
