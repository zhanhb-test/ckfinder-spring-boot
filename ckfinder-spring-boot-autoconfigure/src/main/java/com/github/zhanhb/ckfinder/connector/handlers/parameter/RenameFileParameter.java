package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class RenameFileParameter extends ErrorListXmlParameter {

  private String fileName;
  private String newFileName;
  private boolean renamed;
  private boolean addRenameNode;

}
