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
package com.github.zhanhb.ckfinder.connector.errors;

import com.github.zhanhb.ckfinder.connector.configuration.ConnectorError;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import java.util.Objects;

/**
 * Connector Exception.
 */
public class ConnectorException extends Exception {

  private static final long serialVersionUID = -8643752550259111562L;

  private final ConnectorError errorCode;
  private final String currentFolder;
  private ResourceType type;

  /**
   * standard constructor.
   *
   * @param param the parameters
   * @param code error code number
   */
  public ConnectorException(Parameter param, ConnectorError code) {
    super(null, null);
    this.errorCode = Objects.requireNonNull(code);
    this.currentFolder = param.getCurrentFolder();
    this.type = param.getType();
  }

  /**
   * standard constructor.
   *
   * @param errorCode error code number
   */
  public ConnectorException(ConnectorError errorCode) {
    super(null, null);
    this.errorCode = Objects.requireNonNull(errorCode);
    this.currentFolder = null;
  }

  /**
   * constructor with error code and error message parameters.
   *
   * @param errorCode error code number
   * @param errorMsg error text message
   */
  public ConnectorException(ConnectorError errorCode, String errorMsg) {
    super(errorMsg, null);
    this.errorCode = Objects.requireNonNull(errorCode);
    this.currentFolder = null;
  }

  /**
   * constructor with error code and error message parameters.
   *
   * @param errorCode error code number
   * @param e exception
   */
  public ConnectorException(ConnectorError errorCode, Exception e) {
    super(e.getMessage(), e);
    if (e instanceof ConnectorException) {
      throw new IllegalArgumentException();
    }
    this.errorCode = Objects.requireNonNull(errorCode);
    this.currentFolder = null;
  }

  /**
   * constructor with exception param.
   *
   * @param cause Exception
   */
  @SuppressWarnings("deprecation")
  public ConnectorException(Exception cause) {
    super(cause.getMessage(), cause);
    if (cause instanceof ConnectorException) {
      throw new IllegalArgumentException();
    }
    this.currentFolder = null;
    this.errorCode = ConnectorError.UNKNOWN;
  }

  public ConnectorError getErrorCode() {
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
