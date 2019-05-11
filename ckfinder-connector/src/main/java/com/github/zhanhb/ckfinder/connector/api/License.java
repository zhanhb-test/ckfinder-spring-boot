package com.github.zhanhb.ckfinder.connector.api;

import lombok.Builder;
import lombok.Value;

/**
 * License name and key.
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@Value
public class License {

  private String name;
  private String key;

}
