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
public class CopyMoveParameter extends ErrorListXmlParameter {

  private final List<FilePostParam> files = new ArrayList<>();
  private int nfiles;
  private int all;

  public void increase() {
    nfiles++;
  }

}
