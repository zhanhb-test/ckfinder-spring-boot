package com.github.zhanhb.ckfinder.connector.configuration;

import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author zhanhb
 */
@RequiredArgsConstructor
public class FixLicenseFactory implements LicenseFactory {

  private final License license;

  @Override
  public License getLicense(HttpServletRequest request) {
    return license;
  }

}
