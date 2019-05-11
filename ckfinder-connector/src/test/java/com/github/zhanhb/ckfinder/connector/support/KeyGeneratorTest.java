package com.github.zhanhb.ckfinder.connector.support;

import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class KeyGeneratorTest {

  /**
   * Test of generateKey method, of class KeyGenerator.
   */
  @Test
  public void testGenerateKey() {
    log.info("generateKey");
    boolean host = true;
    String licenseName = "localhost";
    KeyGenerator instance = KeyGenerator.INSTANCE;
    String result = instance.generateKey(licenseName, host);
    log.info(result);
  }

  @Test
  public void testR() {
    log.info("r");
    KeyGenerator instance = KeyGenerator.INSTANCE;
    int n = 32;
    int[] a = new int[n];
    for (int i = 0; i < 1_000_000; ++i) {
      ++a[instance.r(8, i % 8, n)];
    }
    assertTrue(a[n - 1] > 0);

    for (int i = 0; i < 1_000_000; ++i) {
      final int div = ThreadLocalRandom.current().nextInt(99999999) + 1;
      final int rem = ThreadLocalRandom.current().nextInt(99999999);
      final int lim = ThreadLocalRandom.current().nextInt(99999999 - rem) + rem + 1;
      int t = instance.r(div, rem, lim);
      assertEquals(rem % div, t % div);
      assertTrue(0 <= t && t < lim);
    }
  }

}
