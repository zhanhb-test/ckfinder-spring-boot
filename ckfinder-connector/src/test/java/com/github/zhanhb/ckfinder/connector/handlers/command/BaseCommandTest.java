package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
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

}
