package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.support.FileItem;
import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Value
@SuppressWarnings("FinalClass")
public class CopyMoveParameter {

  private final List<FileItem> files;
  private final int all;

}
