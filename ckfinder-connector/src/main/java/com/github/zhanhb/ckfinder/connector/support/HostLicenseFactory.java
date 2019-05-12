package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.License;
import com.github.zhanhb.ckfinder.connector.api.LicenseFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nonnull;

/**
 *
 * @author zhanhb
 */
public class HostLicenseFactory implements LicenseFactory {

  private final ConcurrentMap<Integer, String> keys = new ConcurrentHashMap<>(4);

  @Override
  @Nonnull
  public License getLicense(String host) {
    String key = getKey(host);
    return License.builder().name(host).key(key).build();
  }

  private String getKey(String name) {
    return keys.computeIfAbsent(name.length(), __ -> generateKey(name));
  }

  private String generateKey(String name) {
    return KeyGenerator.INSTANCE.generateKey(name, true);
  }

}
