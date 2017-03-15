package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class DownloadFileParameter extends Parameter {

  /**
   * filename request param.
   */
  private String fileName;

}
