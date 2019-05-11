package com.github.zhanhb.ckfinder.connector.support;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 *
 * @author zhanhb
 */
public class CommandFactoryTest {

  /**
   * Test of enableDefaultCommands method, of class DefaultCommandFactory.
   */
  @Test
  public void testEnableDefaultCommands() {
    assertNotNull(new CommandFactoryBuilder().enableDefaultCommands().build().getCommand("INIT"));
  }

}
