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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Error utils.
 */
public enum MessageUtil {

  INSTANCE;

  private static final String BUNDLE_NAME = MessageUtil.class.getPackage()
          .getName().concat(".LocalStrings");
  private static final Logger log = LoggerFactory.getLogger(MessageUtil.class);
  private final ConcurrentMap<String, Locale> map = new ConcurrentHashMap<>(16);

  /**
   * Gets error message by locale code.
   *
   * @param errorCode error number
   * @param lang connector language code
   * @return localized error message.
   */
  public String getMessage(String lang, int errorCode) {
    try {
      return ResourceBundle.getBundle(BUNDLE_NAME, getLocale(lang)).getString(Integer.toString(errorCode));
    } catch (RuntimeException ex) {
      return "";
    }
  }

  private Locale getLocale(String lang) {
    return map.computeIfAbsent(lang, lang1 -> {
      String[] split = lang1.split("[-_]", 3);
      switch (split.length) {
        case 1:
          return new Locale(split[0]);
        case 2:
          return new Locale(split[0], split[1]);
        case 3:
          return new Locale(split[0], split[1], split[2]);
      }
      log.error("unknown locale '{}'", lang1);
      return Locale.ROOT;
    });
  }

}
