package com.github.zhanhb.ckfinder.connector.support;

import java.util.Map;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@Value
class PluginInfo {

  private String name;
  @Singular
  private Map<String, String> params;

}
