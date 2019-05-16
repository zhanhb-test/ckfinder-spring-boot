package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.support.DefaultCKFinderContext;

@FunctionalInterface
public interface CKFinderContextCustomizer {

  DefaultCKFinderContext.Builder custom(DefaultCKFinderContext.Builder builder);

}
