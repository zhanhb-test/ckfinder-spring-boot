/*
 * Copyright 2017 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zhanhb.ckfinder.download;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.function.Function;

/**
 *
 * @author zhanhb
 */
public class ETagStrategy {

  public static final Function<PartialContext, String> DEFAULT_ETAG_MAPPER
          = context -> {
            BasicFileAttributes attributes = context.getAttributes();
            return attributes.size() + "-" + attributes.lastModifiedTime().toMillis();
          };

  public static ETagStrategy weak() {
    return weak(DEFAULT_ETAG_MAPPER);
  }

  public static ETagStrategy strong() {
    return strong(DEFAULT_ETAG_MAPPER);
  }

  public static ETagStrategy weak(Function<PartialContext, String> eTagMapper) {
    return new ETagStrategy(true, eTagMapper);
  }

  public static ETagStrategy strong(Function<PartialContext, String> eTagMapper) {
    return new ETagStrategy(false, eTagMapper);
  }

  private final String prefix;
  private final Function<PartialContext, String> eTagMapper;

  private ETagStrategy(boolean weak, Function<PartialContext, String> eTagMapper) {
    this.prefix = weak ? "W/" : "";
    this.eTagMapper = Objects.requireNonNull(eTagMapper, "eTagMapper");
  }

  public String apply(PartialContext context) {
    String result = Objects.requireNonNull(eTagMapper.apply(context));
    return prefix + "\"" + result + "\"";
  }

}
