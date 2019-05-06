package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FileItem;
import java.util.List;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Getter
public class DeleteFilesParameter extends ErrorListXmlParameter {

  private final List<FileItem> files;
  private int filesDeleted;

  public DeleteFilesParameter(List<FileItem> files) {
    this.files = files;
  }

  public void filesDeletedPlus() {
    filesDeleted++;
  }

}
