/*
 * Copyright 2015 zhanhb.
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

import java.util.Optional;

import static com.github.zhanhb.ckfinder.download.ContentDispositionEncoder.wrapper;

/**
 *
 * @see <a href="https://tools.ietf.org/html/rfc6266">rfc6266</a>
 * @author zhanhb
 */
public interface ContentDispositionStrategy extends Strategy<Optional<String>> {

  NameMapper DEFAULT_NAME_MAPPER = path -> path.getFileName().toString();

  static ContentDispositionStrategy attachment() {
    return attachment(DEFAULT_NAME_MAPPER);
  }

  static ContentDispositionStrategy inline() {
    return inline(DEFAULT_NAME_MAPPER);
  }

  static ContentDispositionStrategy attachment(NameMapper nameMapper) {
    return wrapper("attachment", nameMapper);
  }

  static ContentDispositionStrategy inline(NameMapper nameMapper) {
    return wrapper("inline", nameMapper);
  }

  static ContentDispositionStrategy none() {
    return __ -> Optional.empty();
  }

  @Override
  Optional<String> apply(PartialContext context);

}
