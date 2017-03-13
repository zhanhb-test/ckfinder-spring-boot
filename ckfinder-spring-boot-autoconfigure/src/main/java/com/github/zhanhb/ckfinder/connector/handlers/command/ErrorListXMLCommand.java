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
package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.ErrorListXMLArguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import java.util.function.Supplier;

/**
 * Base class to handle XML commands with error list.
 *
 * @param <T>
 */
@SuppressWarnings("FinalMethod")
public abstract class ErrorListXMLCommand<T extends ErrorListXMLArguments> extends XMLCommand<T> {

  public ErrorListXMLCommand(Supplier<T> argumentsSupplier) {
    super(argumentsSupplier);
  }

  @Override
  protected final void createXml(T arguments, IConfiguration configuration) throws ConnectorException {
    int errorNum = getDataForXml(arguments, configuration);
    arguments.setErrorNum(errorNum);
  }

  @Override
  protected final void createErrorNode(Connector.Builder rootElement, T arguments) {
    int errorNum = arguments.getErrorNum();
    rootElement.error(Error.builder().number(errorNum).build());
  }

  /**
   * gets all necessary data to create XML response.
   *
   * @param arguments
   * @param configuration connector configuration
   * @return error code
   * {@link com.github.zhanhb.ckfinder.connector.configuration.Constants.Errors}
   * or
   * {@link com.github.zhanhb.ckfinder.connector.configuration.Constants.Errors#CKFINDER_CONNECTOR_ERROR_NONE}
   * if no error occurred.
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  protected abstract int getDataForXml(T arguments, IConfiguration configuration) throws ConnectorException;

}
