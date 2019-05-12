package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Getter
public class RenameFileParameter {

  private String fileName;
  private String newFileName;

}
