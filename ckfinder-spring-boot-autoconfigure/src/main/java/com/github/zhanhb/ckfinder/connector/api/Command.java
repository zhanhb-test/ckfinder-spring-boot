package com.github.zhanhb.ckfinder.connector.api;

import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 */
public interface Command {

  /**
   * Runs command. Initialize, sets response and execute command.
   *
   * @param request request
   * @param response response
   * @param configuration connector configuration
   * @throws ConnectorException when error occurred.
   * @throws java.io.IOException
   */
  void runCommand(HttpServletRequest request, HttpServletResponse response,
          Configuration configuration) throws ConnectorException, IOException;

}
