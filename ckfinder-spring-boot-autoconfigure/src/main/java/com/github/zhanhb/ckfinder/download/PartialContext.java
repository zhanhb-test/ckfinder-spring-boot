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

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter(AccessLevel.PACKAGE)
public class PartialContext {

  private HttpServletRequest request;
  private HttpServletResponse response;
  private ServletContext servletContext;
  private Path path;
  private BasicFileAttributes attributes;

  public PartialContext(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, Path path) {
    this.request = request;
    this.response = response;
    this.servletContext = servletContext;
    this.path = path;
  }

}
