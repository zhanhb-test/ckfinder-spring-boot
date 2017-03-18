package com.github.zhanhb.ckfinder.connector.errors;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 */
public interface ExceptionHandler {

  void handleException(HttpServletRequest request, HttpServletResponse response, IConfiguration configuration, ConnectorException connectorException) throws IOException;

}
