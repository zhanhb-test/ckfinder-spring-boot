package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class RenameFolderParameter extends Parameter {

  private String newFolderName;
  private String newFolderPath;

}
