package com.github.zhanhb.ckfinder.connector.configuration;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Getter
@RequiredArgsConstructor
public class License {

  private final String name;
  private final String key;

}
