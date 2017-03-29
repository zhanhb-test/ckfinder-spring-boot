package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class SaveFileParameter extends Parameter {

  private String fileName;
  private String fileContent;

}
