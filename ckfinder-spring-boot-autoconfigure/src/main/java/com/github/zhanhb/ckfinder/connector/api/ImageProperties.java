package com.github.zhanhb.ckfinder.connector.api;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Getter
public class ImageProperties {

  private final int maxWidth;
  private final int maxHeight;
  private final float quality;

  @SuppressWarnings("PublicInnerClass")
  public static class Builder {

    Builder() {

      maxWidth = Constants.DEFAULT_IMG_WIDTH;
      maxHeight = Constants.DEFAULT_IMG_HEIGHT;
      quality = Constants.DEFAULT_IMG_QUALITY;
    }

  }

}
