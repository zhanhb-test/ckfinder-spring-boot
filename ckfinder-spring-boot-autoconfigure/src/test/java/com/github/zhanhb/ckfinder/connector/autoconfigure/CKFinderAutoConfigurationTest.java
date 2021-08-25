package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 *
 * @author zhanhb
 */
public class CKFinderAutoConfigurationTest {

  private AnnotationConfigWebApplicationContext context;

  @BeforeEach
  public void init() {
    context = new AnnotationConfigWebApplicationContext();
  }

  @AfterEach
  public void closeContext() {
    if (this.context != null) {
      this.context.close();
    }
  }

  @Test
  public void test() {
    context.register(MockServletContextConfiguration.class);
    context.refresh();
    context.getBean(CKFinderContext.class);
  }

}
