package com.github.zhanhb.ckfinder.connector.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author zhanhb
 */
@Slf4j
public class AclContextTest {

  /**
   * Test of size method, of class AclContext.
   */
  @Test
  public void testSize() {
    log.info("size");
    AclContext instance = new AclContext();
    assertEquals(1, instance.size());
  }

  /**
   * Test of getMask method, of class AclContext.
   */
  @Test
  public void testGetMask() {
    log.info("getMask");
    AclContext instance = new AclContext();
    instance.getRoot().setValue(4);
    assertEquals(4, instance.getMask("/test").getEffectiveValue());
  }

  /**
   * Test of getMask method, of class AclContext.
   */
  @Test
  public void testClosest() {
    log.info("closest");
    AclContext instance = new AclContext();
    Mask root = instance.getRoot();
    root.setValue(4);
    Mask closest1 = instance.closest("/test/abc/");
    Mask closest2 = instance.closest("/test/abc/tt");
    Mask closest3 = instance.closest("/test/abc");

    assertSame(root, closest1);
    assertSame(root, closest2);
    assertSame(root, closest3);
  }

}
