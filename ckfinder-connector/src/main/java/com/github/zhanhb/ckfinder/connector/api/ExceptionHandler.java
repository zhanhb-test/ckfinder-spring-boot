package com.github.zhanhb.ckfinder.connector.api;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 */
public interface ExceptionHandler {

  void handleException(HttpServletRequest request, HttpServletResponse response,
          CKFinderContext context, ConnectorException connectorException) throws IOException;

}
