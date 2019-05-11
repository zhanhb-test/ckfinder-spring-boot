package com.github.zhanhb.ckfinder.connector.plugins;

import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author zhanhb
 */
public enum ImageResizeParam {

  SMALL_THUMB("smallThumb", "small", 90),
  MEDIUM_THUMB("mediumThumb", "medium", 120),
  LARGE_THUMB("largeThumb", "large", 180);

  public static Map<ImageResizeParam, ImageResizeSize> createDefaultParams() {
    Map<ImageResizeParam, ImageResizeSize> map = new EnumMap<>(ImageResizeParam.class);
    for (ImageResizeParam value : ImageResizeParam.values()) {
      map.put(value, value.getDefaultSize());
    }
    return map;
  }

  private final String xmlKey;
  private final String parameterName;
  private final ImageResizeSize defaultSize;

  ImageResizeParam(String xmlKey, String parameter, int defaultSize) {
    this.xmlKey = xmlKey;
    this.parameterName = parameter;
    this.defaultSize = new ImageResizeSize(defaultSize, defaultSize);
  }

  private ImageResizeSize getDefaultSize() {
    return defaultSize;
  }

  String getXmlKey() {
    return xmlKey;
  }

  String getParameter() {
    return parameterName;
  }

}
