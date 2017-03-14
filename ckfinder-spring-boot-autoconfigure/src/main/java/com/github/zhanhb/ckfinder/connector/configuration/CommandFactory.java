package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.handlers.command.Command;
import java.util.Map;

/**
 *
 * @author zhanhb
 */
public class CommandFactory {

  private final Map<String, Command<?>> commands;

  CommandFactory(Map<String, Command<?>> commands) {
    this.commands = commands;
  }

  public Command<?> getCommand(String commandName) {
    return commands.get(commandName);
  }

}
