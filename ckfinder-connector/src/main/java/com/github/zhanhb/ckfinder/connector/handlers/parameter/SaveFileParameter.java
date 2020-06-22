package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Getter
public class SaveFileParameter {

  @Nullable
  private final String fileName;
  @Nullable
  private final String fileContent;

}
