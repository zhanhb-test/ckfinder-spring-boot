package com.github.zhanhb.ckfinder.connector.plugins;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.util.Assert;

/**
 *
 * @author zhanhb
 */
public class ImageResizeSize {

  private int width, height;
  private String strVal;

  public ImageResizeSize(String value) {
    Assert.hasText(value, "thumbs size should not be empty");
    Matcher matcher = Pattern.compile("(\\d+)x(\\d+)").matcher(value);
    if (matcher.matches()) {
      try {
        width = Integer.parseInt(matcher.group(1));
        height = Integer.parseInt(matcher.group(2));
        this.strVal = width + "x" + height;
        return;
      } catch (NumberFormatException ex) {
      }
    }
    throw new IllegalArgumentException(value);
  }

  ImageResizeSize(int width, int height) {
    this.width = width;
    this.height = height;
    this.strVal = width + "x" + height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public String toString() {
    return strVal;
  }

}
