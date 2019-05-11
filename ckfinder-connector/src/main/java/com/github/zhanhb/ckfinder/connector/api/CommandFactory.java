package com.github.zhanhb.ckfinder.connector.api;

/**
 * Storage of all commands.
 *
 * @author zhanhb
 */
public interface CommandFactory {

  Command getCommand(String name);

}
