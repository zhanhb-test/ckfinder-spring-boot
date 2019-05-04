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

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.ErrorListXmlParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;

/**
 * Base class to handle XML commands with error list.
 *
 * @param <T> parameter type
 */
public abstract class ErrorListXmlCommand<T extends ErrorListXmlParameter> extends XmlCommand<T> {

  @Override
  @SuppressWarnings("FinalMethod")
  final Connector buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException {
    Connector.Builder connector = Connector.builder();
    ErrorCode error = getDataForXml(param, cmdContext);
    int errorCode = error != null ? error.getCode() : 0;
    cmdContext.setResourceType(connector);
    createCurrentFolderNode(cmdContext, connector);
    createErrorNode(connector, errorCode);
    param.addErrorsTo(connector);
    if (param.isAddResultNode()) {
      addResultNode(connector, param);
    }
    return connector.build();
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param param the parameter
   * @param context ckfinder context
   */
  protected abstract void addResultNode(Connector.Builder rootElement, T param);

  /**
   * gets all necessary data to create XML response.
   *
   * @param param the parameter
   * @param context ckfinder context
   * @return the warning code or null if it's correct.
   * {@link com.github.zhanhb.ckfinder.connector.api.ErrorCode} if no error
   * occurred.
   * @throws ConnectorException when error occurs
   */
  protected abstract ErrorCode getDataForXml(T param, CommandContext cmdContext) throws ConnectorException;

}
