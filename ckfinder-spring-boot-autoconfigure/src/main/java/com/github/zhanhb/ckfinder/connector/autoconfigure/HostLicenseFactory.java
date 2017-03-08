package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.configuration.KeyGenerator;
import com.github.zhanhb.ckfinder.connector.configuration.License;
import com.github.zhanhb.ckfinder.connector.configuration.LicenseFactory;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author zhanhb
 */
class HostLicenseFactory implements LicenseFactory {

  @Override
  public License getLicense(HttpServletRequest request) {
    String name = request.getServerName();
    String key = KeyGenerator.INSTANCE.generateKey(true, name, 34);
    return License.builder().name(name).key(key).build();
  }

}
