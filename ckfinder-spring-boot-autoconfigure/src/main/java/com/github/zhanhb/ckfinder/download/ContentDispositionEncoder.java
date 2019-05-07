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

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.ContentDisposition;

/**
 * @see <a href="https://tools.ietf.org/html/rfc6266#section-4.1">
 * RFC6266#section-4.1</a>
 * @author zhanhb
 */
interface ContentDispositionEncoder {

  static ContentDispositionStrategy wrapper(String type, NameMapper nameMapper) {
    Objects.requireNonNull(nameMapper, "nameMapper");
    return context -> Optional.ofNullable(nameMapper.apply(context.getPath()))
            .map(filename -> ContentDisposition.builder(type)
            .filename(filename, StandardCharsets.UTF_8)
            .build().toString()).orElse(null);
  }

}
