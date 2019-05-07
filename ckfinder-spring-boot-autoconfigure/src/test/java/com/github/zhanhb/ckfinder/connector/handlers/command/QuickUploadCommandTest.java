package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import java.io.StringWriter;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author zhanhb
 */
@Slf4j
public class QuickUploadCommandTest {

  // use null class loader, ensure access of system script engine manager.
  // usually our classloader will extends system class loader.
  // but surefire won't do like this when not forking
  // the enigine manager can be found though system class loader.
  private final ScriptEngine javascript = new ScriptEngineManager(null).getEngineByName("javascript");

  /**
   * Test of writeJSON method, of class QuickUploadCommand.
   */
  @Test
  public void testWriteJSON() throws Exception {
    log.info("writeJSON");
    StringWriter writer = new StringWriter();
    String[] errorMsgs = {"msg", "", null};
    for (String errorMsg : errorMsgs) {
      String[] paths = {"path/", "", null};
      for (String path : paths) {
        String[] fileNames = {"te\"s't.jpg", "", null};
        for (String fileName : fileNames) {
          if (!StringUtils.isEmpty(path) && fileName == null) {
            continue;
          }
          QuickUploadCommand instance = new QuickUploadCommand();
          instance.writeJSON(writer, errorMsg, path, fileName, 0);
          String result = writer.toString();
          writer.getBuffer().setLength(0);
          log.trace("result={}", result);
          String rhino = (String) javascript.eval("JSON.stringify(" + result + ")");
          try {
            assertEquals(rhino, result);
          } catch (AssertionError ae) {
            log.warn(ae.getMessage());
          }
        }
      }
    }
  }

}
