package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.util.Objects;

/**
 *
 * @author zhanhb
 */
class UncheckedConnectorException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private UncheckedConnectorException(ConnectorException cause) {
    super(Objects.requireNonNull(cause));
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

  private void readObject(ObjectInputStream s)
          throws IOException, ClassNotFoundException {
    s.defaultReadObject();
    Throwable cause = super.getCause();
    if (!(cause instanceof ConnectorException)) {
      throw new InvalidObjectException("Cause must be an IOException");
    }
  }

}
