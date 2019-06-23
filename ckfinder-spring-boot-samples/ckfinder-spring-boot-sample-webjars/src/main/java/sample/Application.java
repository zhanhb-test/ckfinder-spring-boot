package sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  private static final Logger log = LoggerFactory.getLogger(Application.class);

  public static void main(String[] args) {
    ApplicationContext context = SpringApplication.run(Application.class, args);
    if (context instanceof WebServerApplicationContext) {
      WebServer webServer = ((WebServerApplicationContext) context).getWebServer();
      log.info("http://localhost:{}{}", webServer.getPort(), context.getApplicationName());
    }
  }

}
