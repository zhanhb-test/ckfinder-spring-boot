package com.github.zhanhb.ckfinder.connector.errors;

import com.github.zhanhb.ckfinder.connector.api.Configuration;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 */
public interface ExceptionHandler {

  void handleException(HttpServletRequest request, HttpServletResponse response, Configuration configuration, ConnectorException connectorException) throws IOException;

}
