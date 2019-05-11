package com.github.zhanhb.ckfinder.connector.handlers.response;

import com.github.zhanhb.ckfinder.connector.api.PluginInfo;
import java.util.Map;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.namespace.QName;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 *
 * @see com.github.zhanhb.ckfinder.connector.plugins.ImageResize
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Value
@SuppressWarnings("FinalClass")
public class ImageResizeInfo implements PluginInfo {

  private String name;

  @Singular
  @XmlAnyAttribute
  private Map<QName, String> attributes;

}
