package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.data.FilePostParam;
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
public class CopyFilesParameter extends ErrorListXmlParameter {

  private final List<FilePostParam> files = new ArrayList<>();
  private int filesCopied;
  private int copiedAll;
  private boolean addCopyNode;

  public void filesCopiedPlus() {
    filesCopied++;
  }

}
