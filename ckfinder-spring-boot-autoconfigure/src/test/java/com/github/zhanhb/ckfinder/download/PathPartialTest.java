package com.github.zhanhb.ckfinder.download;

import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@Slf4j
public class PathPartialTest {

  /**
   * Test of builder method, of class PathPartial.
   */
  @Test
  public void testBuilder() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    log.info("builder");
    Field field = PathPartial.class.getDeclaredField("HAS_METHOD_CONTENT_LENGTH_LONG");
    field.setAccessible(true);
    Object get = field.get(null);
    assertEquals(true, get);
  }

}
