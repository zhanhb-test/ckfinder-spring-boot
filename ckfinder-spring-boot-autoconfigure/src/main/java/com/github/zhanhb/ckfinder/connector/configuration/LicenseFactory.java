package com.github.zhanhb.ckfinder.connector.configuration;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

public interface LicenseFactory {

  @Nonnull
  License getLicense(HttpServletRequest request);

}
