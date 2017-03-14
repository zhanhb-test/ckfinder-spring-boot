package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.configuration.KeyGenerator;
import com.github.zhanhb.ckfinder.connector.configuration.License;
import com.github.zhanhb.ckfinder.connector.configuration.LicenseFactory;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author zhanhb
 */
class HostLicenseFactory implements LicenseFactory {

  private final AtomicReferenceArray<String> keys = new AtomicReferenceArray<>(256);

  @Override
  public License getLicense(HttpServletRequest request) {
    String name = request.getServerName();
    String key = getKey(name);
    return License.builder().name(name).key(key).build();
  }

  private String getKey(String name) {
    int len = name.length();
    if (len < keys.length()) {
      for (;;) {
        String result = keys.get(len);
        if (result != null) {
          return result;
        }
        result = generateKey(name);
        if (keys.compareAndSet(name.length(), null, result)) {
          return result;
        }
      }
    }
    return generateKey(name);
  }

  private String generateKey(String name) {
    return KeyGenerator.INSTANCE.generateKey(name, true);
  }

}
