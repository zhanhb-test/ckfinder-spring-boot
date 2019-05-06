package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Getter
public class CopyMoveParameter extends ErrorListXmlParameter {

  private final List<FilePostParam> files;
  private final int all;
  private int nfiles;

  public CopyMoveParameter(List<FilePostParam> files, int all) {
    this.files = files;
    this.all = all;
  }

  public void increase() {
    nfiles++;
  }

}
