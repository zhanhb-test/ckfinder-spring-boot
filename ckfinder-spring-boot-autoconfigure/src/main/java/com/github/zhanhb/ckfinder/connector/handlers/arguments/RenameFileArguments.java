package com.github.zhanhb.ckfinder.connector.handlers.arguments;

import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class RenameFileArguments extends ErrorListXMLArguments {

  private String fileName;
  private String newFileName;
  private boolean renamed;
  private boolean addRenameNode;

}
