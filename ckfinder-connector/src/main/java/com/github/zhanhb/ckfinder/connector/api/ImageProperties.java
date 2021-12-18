package com.github.zhanhb.ckfinder.connector.api;

import lombok.Builder;
import lombok.Value;

/**
 * Configuration of image properties.
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@Value
public class ImageProperties {

  int maxWidth;
  int maxHeight;
  float quality;

  @SuppressWarnings("PublicInnerClass")
  public static class Builder {

    Builder() {

      maxWidth = Constants.DEFAULT_IMG_WIDTH;
      maxHeight = Constants.DEFAULT_IMG_HEIGHT;
      quality = Constants.DEFAULT_IMG_QUALITY;
    }

  }

}
