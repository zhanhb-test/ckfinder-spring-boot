package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import lombok.Builder;
import lombok.Getter;

/**
 *
 * @author zhanhb
 */
@Builder
@Getter
public class SaveFileParameter {

  private final String fileName;
  private final String fileContent;

}
