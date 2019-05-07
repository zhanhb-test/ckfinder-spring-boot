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

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

/**
 * Utility class calculate path in connector.
 */
@UtilityClass
@SuppressWarnings("FinalClass")
public class PathUtils {

  public String normalize(String string) {
    return string != null ? string.replaceAll("[\\\\/]+", "/") : null;
  }

  /**
   * Escapes double slashes (//) and replaces backslashes characters (\) with
   * slashes (/). <br>
   * <strong>NOTE:</strong> This method preserves UNC paths.
   *
   * @param string string to escapeUrl
   * @return Escaped string, {@code null} or empty string.
   */
  public String normalizeUrl(String string) {
    if (StringUtils.isEmpty(string)) {
      return string;
    }
    final int prefixIndex = string.indexOf("://");
    String prefix;
    String suffix;
    if (prefixIndex > -1) {
      prefix = string.substring(0, prefixIndex + 2);
      suffix = string.substring(prefixIndex + 2);
    } else {
      prefix = "";
      suffix = string;
    }
    suffix = suffix.replace('\\', '/');

    // preserve // at the beginning for UNC paths
    if (suffix.startsWith("//")) {
      return prefix + "/" + suffix.replaceAll("/+", "/");
    } else {
      return prefix.concat(suffix.replaceAll("/+", "/"));
    }
  }

  /**
   * Adds slash character at the end of String provided as parameter. The slash
   * character will not be added if parameter is empty string or ends with
   * slash.
   *
   * @param string string to add slash character to
   * @return String with slash character at the end, {@code null} or empty
   * string.
   */
  public String addSlashToEnd(String string) {
    if (string == null || string.endsWith("/")) {
      return string;
    }
    return string.concat("/");
  }

  /**
   * Adds slash character at the start of String provided as parameter. The
   * slash character will not be added if parameter is ULR or starts with slash.
   *
   * @param string string to add slash character to
   * @return String with slash character at the beginning, {@code null} or full
   * URL.
   */
  public String addSlashToBegin(String string) {
    if (string == null || string.startsWith("/")
            || UrlPatternHolder.URL_PATTERN.matcher(string).matches()) {
      return string;
    }
    return "/".concat(string);
  }

  private interface UrlPatternHolder {

    Pattern URL_PATTERN = Pattern.compile("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$");

  }

}
