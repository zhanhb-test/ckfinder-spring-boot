package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class ImageResizeParameter extends Parameter {

  /**
   * file name
   */
  private String fileName;
  private String newFileName;
  private String overwrite;
  private Integer width;
  private Integer height;
  private boolean wrongReqSizesParams;
  private final Map<String, String> sizesFromReq = new HashMap<>();

}
