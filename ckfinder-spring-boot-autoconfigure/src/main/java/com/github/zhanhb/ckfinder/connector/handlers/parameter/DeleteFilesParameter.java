package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
@SuppressWarnings("CollectionWithoutInitialCapacity")
public class DeleteFilesParameter extends ErrorListXmlParameter {

  private final List<FilePostParam> files = new ArrayList<>();
  private int filesDeleted;

  public void filesDeletedPlus() {
    filesDeleted++;
  }

}
