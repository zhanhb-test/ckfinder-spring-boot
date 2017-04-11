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

/**
 * Connector Exception.
 */
public class ConnectorException extends Exception {

  private static final long serialVersionUID = -8643752550259111562L;

  private final ErrorCode errorCode;
  private final String currentFolder;
  private ResourceType type;

  /**
   * standard constructor.
   *
   * @param type resource type
   * @param currentFolder the parameters
   * @param code error code number
   */
  public ConnectorException(ErrorCode code, ResourceType type, String currentFolder) {
    super(code.name(), null);
    this.errorCode = Objects.requireNonNull(code);
    this.type = type;
    this.currentFolder = currentFolder;
  }

  /**
   * standard constructor.
   *
   * @param code error code number
   */
  public ConnectorException(ErrorCode code) {
    super(code.name(), null);
    this.errorCode = Objects.requireNonNull(code);
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
    this.currentFolder = null;
  }

  /**
   * constructor with error code and error message parameters.
   *
   * @param code error code number
   * @param e exception
   */
  public ConnectorException(ErrorCode code, Exception e) {
    super(e.getMessage(), e);
    if (e instanceof ConnectorException) {
      throw new IllegalArgumentException();
    }
    this.errorCode = Objects.requireNonNull(code);
    this.currentFolder = null;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }

  public String getCurrentFolder() {
    return currentFolder;
  }

  public ResourceType getType() {
    return type;
  }

  @Override
  public Throwable fillInStackTrace() {
    return this;
  }

}
