package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class ImageResizeInfoParameter extends Parameter {

  private int imageWidth;
  private int imageHeight;
  private String fileName;

}
