package com.github.zhanhb.ckfinder.connector.api;

import javax.annotation.Nonnull;

public interface LicenseFactory {

  @Nonnull
  License getLicense(String host);

}
