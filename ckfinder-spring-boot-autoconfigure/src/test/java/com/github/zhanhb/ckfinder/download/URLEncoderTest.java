package com.github.zhanhb.ckfinder.download;

import com.google.common.base.Strings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;

/**
 *
 * @author zhanhb
 */
public class URLEncoderTest {

  @Test
  public void testEncode() {
    String str = "hello, \u4f60\u597d";
    URLEncoder encoder = new URLEncoder(",");
    String expect = "hello,%20%E4%BD%A0%E5%A5%BD";

    assertEquals(expect, encoder.encode(str));
    assertEquals(expect + expect, encoder.encode(str + str));
    assertEquals(expect + expect + 1, encoder.encode(str + str + 1));

    str = Strings.repeat(str, 13);
    assertNotEquals(new URLEncoder("").encode(str), encoder.encode(str));

    str = "abc,,,123456";
    assertSame(str, encoder.encode(str));
  }

}
