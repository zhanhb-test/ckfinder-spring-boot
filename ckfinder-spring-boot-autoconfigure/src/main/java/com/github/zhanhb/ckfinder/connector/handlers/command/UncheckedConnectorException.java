package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;

/**
 *
 * @author zhanhb
 */
class UncheckedConnectorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private UncheckedConnectorException(ConnectorException cause) {
    super(cause);
  }

  UncheckedConnectorException(ErrorCode code) {
    this(new ConnectorException(code));
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public ConnectorException getCause() {
    return (ConnectorException) super.getCause();
  }

}
