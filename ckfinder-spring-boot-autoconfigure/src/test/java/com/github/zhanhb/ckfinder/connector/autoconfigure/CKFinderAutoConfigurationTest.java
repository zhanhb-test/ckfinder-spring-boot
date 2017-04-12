package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 *
 * @author zhanhb
 */
public class CKFinderAutoConfigurationTest {

  private AnnotationConfigWebApplicationContext context;

  @Before
  public void init() {
    context = new AnnotationConfigWebApplicationContext();
  }

  @After
  public void closeContext() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  public void test() {
    context.register(MockServlecContextConfiguration.class);
    context.refresh();
    context.getBean(CKFinderContext.class);
  }

}
