package com.github.zhanhb.ckfinder.connector.api;

/**
 *
 * @author zhanhb
 */
public interface CommandFactory {

  Command getCommand(String commandName);

}
