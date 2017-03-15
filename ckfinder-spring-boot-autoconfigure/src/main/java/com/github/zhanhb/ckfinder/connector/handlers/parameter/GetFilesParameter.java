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
public class GetFilesParameter extends Parameter {

  /**
   * list of files.
   */
  private List<String> files;
  /**
   * temporary field to keep full path.
   */
  private String fullCurrentPath;
  /**
   * show thumb post param.
   */
  private String showThumbs;

}
