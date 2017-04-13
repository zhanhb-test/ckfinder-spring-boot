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
import lombok.Value;
import org.springframework.core.io.InputStreamSource;

@Builder(builderClassName = "Builder")
@SuppressWarnings({"FinalClass", "PublicInnerClass"})
@Value
public class WatermarkSettings {

  private InputStreamSource source;
  private float transparency;
  private float quality;
  private int marginBottom;
  private int marginRight;

  public static class Builder {

    Builder() {
      this.quality = 0.9f;
      this.transparency = 1.0f;
    }

  }

}
