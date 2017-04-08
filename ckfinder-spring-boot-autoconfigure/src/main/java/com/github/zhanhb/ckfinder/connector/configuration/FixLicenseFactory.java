package com.github.zhanhb.ckfinder.connector.configuration;

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 *
 * @author zhanhb
 */
public class FixLicenseFactory implements LicenseFactory {

  private final License license;

  public FixLicenseFactory(License license) {
    this.license = Objects.requireNonNull(license);
  }

  @Override
  @Nonnull
  public License getLicense(String host) {
    return license;
  }

}
