package com.github.zhanhb.ckfinder.connector.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
