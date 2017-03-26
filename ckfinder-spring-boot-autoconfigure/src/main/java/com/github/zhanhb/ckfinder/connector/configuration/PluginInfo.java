package com.github.zhanhb.ckfinder.connector.configuration;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder(builderClassName = "Builder")
@Getter
class PluginInfo {

  private final String name;
  @Singular
  private final Map<String, String> params;

}
