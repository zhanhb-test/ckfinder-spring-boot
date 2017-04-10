package com.github.zhanhb.ckfinder.connector.api;

import java.nio.file.Path;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Getter
@SuppressWarnings({"FinalClass", "PublicInnerClass"})
@Value
public class ThumbnailProperties {

  private String url;
  @NonNull
  private Path path;
  private boolean directAccess;
  private int maxHeight;
  private int maxWidth;
  private float quality;

  public static class Builder {

    public Builder() {
      url = "";
      maxHeight = Constants.DEFAULT_THUMB_MAX_HEIGHT;
      maxWidth = Constants.DEFAULT_THUMB_MAX_WIDTH;
      quality = Constants.DEFAULT_IMG_QUALITY;
    }

  }

}
