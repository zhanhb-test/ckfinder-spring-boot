package com.github.zhanhb.ckfinder.connector.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;

@Slf4j
public class MessageUtilTest {

  /**
   * Test of getMessage method, of class MessageUtil.
   */
  @Test
  public void testGetMessage() {
    log.info("getMessage");
    String lang = "zh-cn";
    int errorCode = 1;
    MessageUtil instance = MessageUtil.INSTANCE;
    String expResult = "";
    String result = instance.getMessage(lang, errorCode);
    assertNotEquals(expResult, result);
  }

}
