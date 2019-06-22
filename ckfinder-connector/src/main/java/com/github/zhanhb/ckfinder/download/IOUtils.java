/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface IOUtils {

  @SuppressWarnings({"NestedAssignment", "ValueOfIncrementOrDecrementUsed"})
  static void copyFully(
          final InputStream input, final OutputStream output,
          final long inputOffset, final long length,
          final byte[] buffer) throws IOException {
    if (length <= 0) {
        throw new IllegalArgumentException();
    }
    if (inputOffset > 0) {
      long remain = inputOffset, n;
      do {
        if ((n = input.skip(remain)) <= 0) {
          throw new EOFException("Bytes to skip: " + inputOffset + " actual: " + (inputOffset - remain));
        }
      } while ((remain -= n) > 0);
    }
    int len = buffer.length, n;
    long rem = length;
    for (;; rem -= n) {
      if (rem < len) {
        for (len = (int) rem; len > 0 && (n = input.read(buffer, 0, len)) != -1; len -= n) {
          output.write(buffer, 0, n);
        }
        rem = len;
        break;
      }
      if ((n = input.read(buffer)) == -1) {
        break;
      }
      output.write(buffer, 0, n);
    }
    if (rem != 0) {
      throw new EOFException("Body changed, bytes to write: " + length + " actual: " + (length - rem));
    }
  }

}
