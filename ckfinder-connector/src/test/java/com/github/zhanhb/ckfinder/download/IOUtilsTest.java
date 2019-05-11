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
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author zhanhb
 */
@Slf4j
public class IOUtilsTest {

  /**
   * Test of copyLarge method, of class IOUtils.
   *
   * @throws java.lang.Exception
   */
  @Test(timeout = 5000)
  @SuppressWarnings("NestedAssignment")
  public void testCopy() throws Exception {
    log.info("copy");
    String str = "abcdefghijklmnopq";
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i <= 8; ++i) {
      for (int j = 0; j <= str.length(); ++j) {
        for (int k = j; k <= str.length() + 1; ++k) {
          final int length = k - j;
          InputStream input = new ByteArrayInputStream(bytes);
          ByteArrayOutputStream output = new ByteArrayOutputStream();
          long copied = IOUtils.copy(input, output, j, length, new byte[i]);
          String expResult = str.substring(j, Math.min(k, str.length()));
          assertArrayEquals(i + " " + j + " " + k, expResult.getBytes(StandardCharsets.UTF_8), output.toByteArray());
          if (length == 0 || k <= str.length()) {
            assertEquals(length, copied);
          } else {
            int expectCopied = str.length() - j;
            assertEquals(expectCopied == 0 ? -1 : expectCopied, copied);
          }
        }
      }
    }
  }

  @Test(timeout = 5000, expected = EOFException.class)
  public void testEofException() throws Exception {
    byte[] bytes = new byte[4];
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    IOUtils.copy(bais, baos, 5, 0, bytes);
  }

  @Test(timeout = 5000)
  public void testNegate() throws Exception {
    String str = "abcdefghijklmnopq";
    byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
    for (int j = 0; j <= str.length(); ++j) {
      for (int i = 1; i <= 8; ++i) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        long copy = IOUtils.copy(bais, baos, j, -1, new byte[i]);
        assertEquals(str.substring(j), baos.toString("UTF-8"));
        int expResult = str.length() - j;
        assertEquals(expResult == 0 ? -1 : expResult, copy);
      }
    }
  }

}
