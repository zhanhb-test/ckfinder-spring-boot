package com.github.zhanhb.ckfinder.connector.configuration;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

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
    int len = 34;
    KeyGenerator instance = new KeyGenerator();
    String result = instance.generateKey(host, licenseName, len);
    log.info(result);
  }

}
