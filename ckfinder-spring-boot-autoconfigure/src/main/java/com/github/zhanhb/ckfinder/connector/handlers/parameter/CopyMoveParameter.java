package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FileItem;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Getter
public class CopyMoveParameter {

  private final List<FileItem> files;
  private final int all;
  private int nfiles;

  public CopyMoveParameter(List<FileItem> files, int all) {
    this.files = files;
    this.all = all;
  }

  public void increase() {
    nfiles++;
  }

}
