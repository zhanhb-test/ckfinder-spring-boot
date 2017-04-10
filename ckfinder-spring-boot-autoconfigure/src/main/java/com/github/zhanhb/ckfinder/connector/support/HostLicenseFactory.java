package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.License;
import com.github.zhanhb.ckfinder.connector.api.LicenseFactory;
import java.util.concurrent.atomic.AtomicReferenceArray;
import javax.annotation.Nonnull;

/**
 *
 * @author zhanhb
 */
public class HostLicenseFactory implements LicenseFactory {

  private final AtomicReferenceArray<String> keys = new AtomicReferenceArray<>(256);

  @Override
  @Nonnull
  public License getLicense(String host) {
    String key = getKey(host);
    return License.builder().name(host).key(key).build();
  }

  private String getKey(String name) {
    int index = name.length();
    if (index < keys.length()) {
      for (;;) {
        String result = keys.get(index);
        if (result != null) {
          return result;
        }
        result = generateKey(name);
        if (keys.compareAndSet(index, null, result)) {
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
