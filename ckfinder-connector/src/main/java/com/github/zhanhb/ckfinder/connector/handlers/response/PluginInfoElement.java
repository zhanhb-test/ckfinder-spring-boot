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
import lombok.NoArgsConstructor;
import lombok.Singular;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@SuppressWarnings({"FinalClass", "WeakerAccess"})
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class PluginInfoElement {

  @Singular
  @XmlAnyAttribute
  private Map<? extends QName, String> attributes;

}
