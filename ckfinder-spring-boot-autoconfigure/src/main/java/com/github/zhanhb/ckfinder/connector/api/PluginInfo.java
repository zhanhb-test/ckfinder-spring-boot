package com.github.zhanhb.ckfinder.connector.api;

import java.util.Map;
import javax.xml.namespace.QName;

/**
 *
 * @author zhanhb
 */
public interface PluginInfo {

  String getName();

  Map<QName, String> getAttributes();

}
