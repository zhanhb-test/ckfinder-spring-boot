package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

@Slf4j
public class BaseCommandTest {

  /**
   * Test of checkRequestPath method, of class BaseCommand.
   */
  @Test
  public void testCheckRequestPath() throws Exception {
    log.info("checkRequestPath");
    String[] success = {
      "/normal",
      "fuck--/z.m",
      "/z../a",};
    String[] failure = {
      "\\",
      "\\../a",
      "/../a",};
    for (String succes : success) {
      BaseCommand.checkRequestPath(succes);
    }
    for (String string : failure) {
      try {
        BaseCommand.checkRequestPath(string);
        fail(string);
      } catch (ConnectorException ex) {
        // ok
      }
    }
  }

  @Test
  public void test1() {
    Path path = Paths.get("/Users/zhahb");
    Path path1 = path.getFileSystem().getPath(path.toString(), "/");
    System.out.println(path.resolveSibling("/"));
    System.out.println(path1);
  }

}
