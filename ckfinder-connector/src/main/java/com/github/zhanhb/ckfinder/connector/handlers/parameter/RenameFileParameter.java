package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class RenameFileParameter {

  private String fileName;
  private String newFileName;
  private boolean renamed;

}
