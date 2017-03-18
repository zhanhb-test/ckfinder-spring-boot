package com.github.zhanhb.ckfinder.connector.configuration;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;

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
  public License getLicense(HttpServletRequest request) {
    return license;
  }

}
