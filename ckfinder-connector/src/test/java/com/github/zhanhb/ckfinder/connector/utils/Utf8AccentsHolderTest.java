package com.github.zhanhb.ckfinder.connector.utils;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@Slf4j
public class Utf8AccentsHolderTest {

  /**
   * Test of convert method, of class Utf8AccentsHolder.
   */
  @Test
  public void testConvert() {
    log.info("convert");
    Random random = ThreadLocalRandom.current();
    for (int k = 0; k < 10000; ++k) {
      int len = 4;

      StringBuilder sb = new StringBuilder(len);
      for (int i = 0; i < len; ++i) {
        if (random.nextBoolean()) {
          sb.append('I');
        } else {
          sb.append((char) 207);
        }
      }
      String raw = sb.toString();
      String result = Utf8AccentsHolder.convert(raw);
      assertEquals(len, result.length());
      assertTrue(result, result.matches("I+"));
      assertSame(result, Utf8AccentsHolder.convert(result));

      assertSame("", Utf8AccentsHolder.convert(""));
    }
  }

}
