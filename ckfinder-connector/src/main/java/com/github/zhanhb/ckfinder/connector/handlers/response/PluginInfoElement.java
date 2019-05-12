package com.github.zhanhb.ckfinder.connector.handlers.response;

import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
@Value
public class PluginInfoElement {

  @Singular
  @XmlAnyAttribute
  private Map<QName, String> attributes;

}
