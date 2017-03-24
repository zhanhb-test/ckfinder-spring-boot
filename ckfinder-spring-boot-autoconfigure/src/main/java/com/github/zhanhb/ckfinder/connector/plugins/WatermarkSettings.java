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
package com.github.zhanhb.ckfinder.connector.plugins;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.io.InputStreamSource;

@Builder(builderClassName = "Builder")
@Getter
public class WatermarkSettings {

  private final InputStreamSource source;
  private final float transparency;
  private final float quality;
  private final int marginBottom;
  private final int marginRight;

  @SuppressWarnings("PublicInnerClass")
  public static class Builder {

    Builder() {
      this.source = null;
      this.marginRight = 0;
      this.marginBottom = 0;
      this.quality = 90;
      this.transparency = 1.0f;
    }

  }

}
