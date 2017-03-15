package com.github.zhanhb.ckfinder.connector.handlers.arguments;

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
public class DeleteFilesArguments extends ErrorListXMLArguments {

  private List<FilePostParam> files = new ArrayList<>();
  private int filesDeleted;
  private boolean addDeleteNode;

  public void filesDeletedPlus() {
    filesDeleted++;
  }

}
