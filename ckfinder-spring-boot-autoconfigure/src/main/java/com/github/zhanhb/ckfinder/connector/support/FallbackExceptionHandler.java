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
package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ExceptionHandler;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to handle errors via HTTP headers (for non-XML commands).
 */
public enum FallbackExceptionHandler implements ExceptionHandler {

  INSTANCE;

  @Override
  public void handleException(HttpServletRequest request,
          HttpServletResponse response, Configuration configuration,
          ConnectorException connectorException) throws IOException {
    ErrorCode errorCode = connectorException.getErrorCode();
    try {
      response.reset();
    } catch (IllegalStateException ex) {
      return;
    }
    response.setIntHeader("X-CKFinder-Error", errorCode.getCode());
    switch (errorCode) {
      case INVALID_REQUEST:
      case INVALID_NAME:
      case THUMBNAILS_DISABLED:
      case UNAUTHORIZED:
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        break;
      case ACCESS_DENIED:
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        break;
      default:
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
        break;
    }
  }

}
