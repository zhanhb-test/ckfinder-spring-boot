package com.github.zhanhb.ckfinder.connector.autoconfigure;

import javax.servlet.ServletContext;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 * @author zhanhb
 */
@Configuration
@EnableAutoConfiguration
@WebAppConfiguration
public class MockServlecContextConfiguration {

  @Bean
  public ServletContext servletContext() {
    return new MockServletContext("target/test", new FileSystemResourceLoader());
  }

}
