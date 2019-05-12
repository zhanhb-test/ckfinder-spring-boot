/*
 * CKFinder
 * ========
 * http://cksource.com/ckfinder
 * Copyright (C) 2007-2015, CKSource - Frederico Knabben. All rights reserved.
 *
 * The software, this file and its contents are subject to the CKFinder
 * License. Please read the license.txt file before using, installing, copying,
 * modifying or distribute this file or part of its contents. The contents of
 * this file is part of the Source Code of CKFinder.
 */
package com.github.zhanhb.ckfinder.connector.api;

import java.util.Objects;
import lombok.Getter;

/**
 * Connector Exception.
 */
@Getter
public class ConnectorException extends Exception {

  private static final long serialVersionUID = -8643752550259111562L;

  private final ErrorCode errorCode;
  private final ResourceType type;
  private final String currentFolder;

  /**
   * standard constructor.
   *
   * @param type resource type
   * @param currentFolder the parameters
   * @param code error code number
   */
  public ConnectorException(ErrorCode code, ResourceType type, String currentFolder) {
    super(code.name());
    this.errorCode = code;
    this.type = type;
    this.currentFolder = currentFolder;
  }

  /**
   * standard constructor.
   *
   * @param code error code number
   */
  public ConnectorException(ErrorCode code) {
    super(code.name());
    this.errorCode = code;
    this.type = null;
    this.currentFolder = null;
  }

  /**
   * constructor with error code and error message parameters.
   *
   * @param code error code number
   * @param errorMsg error text message
   */
  public ConnectorException(ErrorCode code, String errorMsg) {
    super(errorMsg, null);
    this.errorCode = Objects.requireNonNull(code);
    this.type = null;
    this.currentFolder = null;
  }

  /**
   * constructor with error code and error message parameters.
   *
   * @param code error code number
   * @param cause cause
   */
  public ConnectorException(ErrorCode code, Throwable cause) {
    super(cause.getMessage(), cause);
    if (cause instanceof ConnectorException) {
      throw new IllegalArgumentException();
    }
    this.errorCode = Objects.requireNonNull(code);
    this.type = null;
    this.currentFolder = null;
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

  @Override
  public ConnectorException initCause(Throwable cause) {
    super.initCause(cause);
    return this;
  }

}
