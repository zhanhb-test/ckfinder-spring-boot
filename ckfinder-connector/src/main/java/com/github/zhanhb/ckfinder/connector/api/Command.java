package com.github.zhanhb.ckfinder.connector.api;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base interface for all command handlers.
 *
 * @author zhanhb
 */
public interface Command {

  /**
   * Runs command. execute and write result to response.
   *
   * @param request request
   * @param response response
   * @param context ckfinder context
   * @throws ConnectorException when error occurred.
   * @throws IOException when IO Exception occurs.
   */
  void runCommand(HttpServletRequest request, HttpServletResponse response,
          CKFinderContext context) throws ConnectorException, IOException;

}
