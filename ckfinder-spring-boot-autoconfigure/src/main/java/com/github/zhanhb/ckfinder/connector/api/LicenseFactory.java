package com.github.zhanhb.ckfinder.connector.api;

import javax.annotation.Nonnull;

/**
 * License storage
 *
 * @author zhanhb
 */
public interface LicenseFactory {

  @Nonnull
  License getLicense(String host);

}
