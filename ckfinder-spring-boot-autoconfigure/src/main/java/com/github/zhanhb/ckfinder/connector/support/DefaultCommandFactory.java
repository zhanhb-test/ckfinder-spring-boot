package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.Command;
import com.github.zhanhb.ckfinder.connector.api.CommandFactory;
import java.util.Map;

class DefaultCommandFactory implements CommandFactory {

  private final Map<String, ? extends Command> commands;

  DefaultCommandFactory(Map<String, ? extends Command> commands) {
    this.commands = commands;
  }

  @Override
  public Command getCommand(String name) {
    return commands.get(name);
  }

}
