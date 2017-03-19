package com.github.zhanhb.ckfinder.connector.autoconfigure;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 * @author zhanhb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@WebAppConfiguration
public class CKFinderAutoConfigurationTest {

  private AnnotationConfigApplicationContext context;

  @Before
  public void init() {
    context = new AnnotationConfigApplicationContext();
  }

  @After
  public void closeContext() {
    context.close();
  }

  @Test
  public void test() {
    context.register(CKFinderAutoConfigurationAutoConfiguration.class);
    context.refresh();
  }

  @Configuration
  @SpringBootConfiguration
  @SuppressWarnings({"PackageVisibleInnerClass", "ClassMayBeInterface"})
  static class CKFinderAutoConfigurationAutoConfiguration {
  }

}
