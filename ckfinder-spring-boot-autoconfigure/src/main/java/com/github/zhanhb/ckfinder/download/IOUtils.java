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

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class IOUtils {

  @SuppressWarnings({"NestedAssignment", "ValueOfIncrementOrDecrementUsed"})
  public static long copy(final InputStream input, final OutputStream output,
          final long inputOffset, final long length,
          final byte[] buffer) throws IOException {
    if (inputOffset > 0) {
      long remain = inputOffset, n;
      do {
        if ((n = input.skip(remain)) <= 0) {
          throw new EOFException("Bytes to skip: " + inputOffset + " actual: " + (inputOffset - remain));
        }
      } while ((remain -= n) > 0);
    }
    if (length <= 0) {
      if (length != 0) {
        long count = 0;
        for (int n; (n = input.read(buffer)) != -1; count += n) {
          output.write(buffer, 0, n);
        }
        return count == 0 ? -1 : count;
      }
      return 0;
    }
    int len = buffer.length, n;
    long rem = length;
    for (; rem >= len; rem -= n) {
      if ((n = input.read(buffer)) == -1) {
        return length == rem ? -1 : length - rem;
      }
      output.write(buffer, 0, n);
    }
    len = (int) rem;
    for (; len > 0
            && (n = input.read(buffer, 0, len)) != -1; len -= n) {
      output.write(buffer, 0, n);
    }
    return length == len ? -1 : length - len;
  }

}
