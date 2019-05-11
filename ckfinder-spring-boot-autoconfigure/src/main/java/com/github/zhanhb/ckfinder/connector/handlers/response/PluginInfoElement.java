package com.github.zhanhb.ckfinder.connector.handlers.response;

import java.util.Map;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder(builderClassName = "Builder")
@Value
@SuppressWarnings("FinalClass")
public class PluginInfoElement {

  @Singular
  @XmlAnyAttribute
  private Map<QName, String> attributes;

}
