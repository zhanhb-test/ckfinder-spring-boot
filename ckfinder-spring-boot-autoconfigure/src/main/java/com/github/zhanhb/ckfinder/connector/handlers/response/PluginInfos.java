package com.github.zhanhb.ckfinder.connector.handlers.response;

import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 *
 * @author zhanhb
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "PluginsInfo")
@XmlSeeAlso(PluginInfoElement.class)
public class PluginInfos extends Result implements ConnectorElement {

  @Singular
  @XmlAnyElement
  private List<JAXBElement<PluginInfoElement>> pluginInfos;

  @SuppressWarnings("PublicInnerClass")
  public static class Builder {

    public Builder add(String name, Map<QName, String> attributes) {
      PluginInfoElement pie = PluginInfoElement.builder().attributes(attributes).build();
      return pluginInfo(new JAXBElement<>(QName.valueOf(name), PluginInfoElement.class, pie));
    }
  }

}
