package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;

/**
 *
 * @author zhanhb
 */
class UncheckedConnectorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  UncheckedConnectorException(ConnectorException cause) {
    super(cause);
  }

  UncheckedConnectorException(ConnectorError code) {
    this(new ConnectorException(code));
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public synchronized ConnectorException getCause() {
    return (ConnectorException) super.getCause();
  }

}
