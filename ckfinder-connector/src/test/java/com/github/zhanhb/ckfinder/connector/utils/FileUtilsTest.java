package com.github.zhanhb.ckfinder.connector.utils;

import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.support.DefaultResourceType;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author zhanhb
 */
@Slf4j
public class FileUtilsTest {

  /**
   * Test of renameDoubleExtension method, of class FileUtils.
   */
  @Test
  public void testRenameDoubleExtension() {
    log.debug("renameDoubleExtension");
    ResourceType type = DefaultResourceType.builder()
            .name("test")
            .allowedExtensions("html,htm")
            .path(Paths.get("."))
            .deniedExtensions("exe,jsp").build();
    String fileName = "test.exe.html.jsp.jsp";
    String expResult = "test_exe.html_jsp.jsp";
    String result = FileUtils.renameDoubleExtension(type, fileName);
    assertEquals(expResult, result);
  }

}
