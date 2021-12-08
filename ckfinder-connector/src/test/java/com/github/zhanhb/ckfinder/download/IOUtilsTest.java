/*
 * Copyright 2016 zhanhb.
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author zhanhb
 */
@Slf4j
public class IOUtilsTest {

  /**
   * Test of copyFully method, of class IOUtils.
   *
   * @throws java.lang.Exception
   */
  @Test
  @SuppressWarnings("NestedAssignment")
  public void testCopyFully() throws Exception {
    log.info("copy");
    String str = "abcdefghijklmnopq";
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i <= 8; ++i) {
      for (int j = 0; j <= str.length(); ++j) {
        for (int k = j + 1; k <= str.length() + 1; ++k) {
          final int length = k - j;
          InputStream input = new ByteArrayInputStream(bytes);
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          try {
            IOUtils.copyFully(input, output, j, length, new byte[i]);
            if (k > str.length()) {
              fail();
            }
          } catch (EOFException ex) {
            if (k <= str.length()) {
              throw ex;
            }
          }
          String expResult = str.substring(j, Math.min(k, str.length()));
          assertArrayEquals(expResult.getBytes(StandardCharsets.UTF_8), output.toByteArray(), i + " " + j + " " + k);
        }
      }
    }
  }

  @Test
  public void testEofException() {
    byte[] bytes = new byte[4];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    assertThrows(EOFException.class, () -> IOUtils.copyFully(bais, baos, 4, 1, bytes));
  }

}
