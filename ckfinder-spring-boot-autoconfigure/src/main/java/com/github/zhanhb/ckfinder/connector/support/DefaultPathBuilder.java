/*
 * CKFinder
 * ========
 * http://cksource.com/ckfinder
 * Copyright (C) 2007-2015, CKSource - Frederico Knabben. All rights reserved.
 *
 * The software, this file and its contents are subject to the CKFinder
 * License. Please read the license.txt file before using, installing, copying,
 * modifying or distribute this file or part of its contents. The contents of
 * this file is part of the Source Code of CKFinder.
 */
package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.BasePathBuilder;
import java.nio.file.Path;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Path builder that creates default values of base directory and baseURL.
 */
@Builder(builderClassName = "Builder")
@SuppressWarnings("FinalClass")
@Value
public class DefaultPathBuilder implements BasePathBuilder {

  @NonNull
  private Path basePath;
  @NonNull
  private String baseUrl;

}
