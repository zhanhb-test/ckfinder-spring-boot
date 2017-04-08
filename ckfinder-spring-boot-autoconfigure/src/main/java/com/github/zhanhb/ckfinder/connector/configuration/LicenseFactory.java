package com.github.zhanhb.ckfinder.connector.configuration;

import javax.annotation.Nonnull;

public interface LicenseFactory {

  @Nonnull
  License getLicense(String host);

}
