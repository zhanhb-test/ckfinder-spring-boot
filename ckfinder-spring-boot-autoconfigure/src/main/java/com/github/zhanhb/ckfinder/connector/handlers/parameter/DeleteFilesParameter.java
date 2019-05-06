package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Getter
public class DeleteFilesParameter extends ErrorListXmlParameter {

  private final List<FilePostParam> files;
  private int filesDeleted;

  public DeleteFilesParameter(List<FilePostParam> files) {
    this.files = files;
  }

  public void filesDeletedPlus() {
    filesDeleted++;
  }

}
