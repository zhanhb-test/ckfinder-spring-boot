package com.github.zhanhb.ckfinder.connector.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class MessageUtilTest {

  /**
   * Test of getMessage method, of class MessageUtil.
   */
  @Test
  public void testGetMessage() {
    log.info("getMessage");
    String[] langs = {null, "zh-cn", "null", "***----------"};
    int errorCode = 1;
    MessageUtil instance = MessageUtil.INSTANCE;
    for (String lang : langs) {
      String expResult = "";
      String result = instance.getMessage(lang, errorCode);
      assertNotNull(result);
      assertNotEquals(expResult, result);
    }
  }

}
