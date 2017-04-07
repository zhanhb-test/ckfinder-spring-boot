package com.github.zhanhb.ckfinder.download;

import com.google.common.base.Strings;
import org.junit.Test;

/**
 *
 * @author zhanhb
 */
public class URLEncoderTest {

  @Test
  public void testEncode() {
    String str = "hello, \u4f60\u597d";
    str = Strings.repeat(str, 13);
    new URLEncoder("").encode(str);
  }

}
