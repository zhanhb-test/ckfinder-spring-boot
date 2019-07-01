package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;

/**
 *
 * @author zhanhb
 */
class UncheckedConnectorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  UncheckedConnectorException(ErrorCode code) {
    super(new ConnectorException(code));
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public ConnectorException getCause() {
    return (ConnectorException) super.getCause();
  }

  private void readObject(ObjectInputStream s)
          throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    Throwable cause = super.getCause();
    if (!(cause instanceof ConnectorException)) {
      throw new InvalidObjectException("Cause must be a ConnectorException");
    }
  }

}
