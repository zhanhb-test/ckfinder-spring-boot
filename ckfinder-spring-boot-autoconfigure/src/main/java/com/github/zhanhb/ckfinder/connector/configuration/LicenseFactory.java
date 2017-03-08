package com.github.zhanhb.ckfinder.connector.configuration;

import javax.servlet.http.HttpServletRequest;

public interface LicenseFactory {

  License getLicense(HttpServletRequest request);

}
