/*
 * Copyright 2019 zhanhb.
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
 * @author zhanhb
 */
class DefaultETagStrategy implements ETagStrategy {

  private static final Function<PartialContext, String> DEFAULT_ETAG_MAPPER
          = context -> {
            BasicFileAttributes attributes = context.getAttributes();
            return attributes.size() + "-" + attributes.lastModifiedTime().toMillis();
          };
  private static final ETagStrategy DEFAULT_WEAK = weak(DEFAULT_ETAG_MAPPER);
  private static final ETagStrategy DEFAULT_STRONG = strong(DEFAULT_ETAG_MAPPER);

  static ETagStrategy weak() {
    return DEFAULT_WEAK;
  }

  static ETagStrategy strong() {
    return DEFAULT_STRONG;
  }

  static ETagStrategy weak(Function<PartialContext, String> eTagMapper) {
    return new DefaultETagStrategy(true, eTagMapper);
  }

  static ETagStrategy strong(Function<PartialContext, String> eTagMapper) {
    return new DefaultETagStrategy(false, eTagMapper);
  }

  private final String prefix;
  private final Function<PartialContext, String> eTagMapper;

  private DefaultETagStrategy(boolean weak, Function<PartialContext, String> eTagMapper) {
    this.prefix = weak ? "W/" : "";
    this.eTagMapper = Objects.requireNonNull(eTagMapper, "eTagMapper");
  }

  @Override
  public String apply(PartialContext context) {
    String result = Objects.requireNonNull(eTagMapper.apply(context));
    return prefix + "\"" + result + "\"";
  }

}
