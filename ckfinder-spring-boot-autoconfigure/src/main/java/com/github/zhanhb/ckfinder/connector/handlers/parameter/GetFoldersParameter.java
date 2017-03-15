package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class GetFoldersParameter extends Parameter {

  /**
   * list of subdirectories in directory.
   */
  private List<String> directories;

}
