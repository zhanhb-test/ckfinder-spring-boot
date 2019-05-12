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
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;

/**
 * Base class to handle XML commands with error list.
 *
 * @param <T> parameter type
 */
public abstract class FailAtEndXmlCommand<T> extends XmlCommand<T> {

  @Override
  @SuppressWarnings("FinalMethod")
  final ConnectorElement buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException {
    ConnectorElement.Builder connector = ConnectorElement.builder();
    cmdContext.setResourceType(connector);
    ErrorListResult result = applyData(param, cmdContext);
    ErrorCode error = result.getErrorCode();
    int errorCode = error != null ? error.getCode() : 0;
    createCurrentFolderNode(cmdContext, connector);
    createErrorNode(connector, errorCode);
    result.addErrorsTo(connector);
    if (result.isAddResultNode()) {
      addResultNode(connector, param);
    }
    return connector.build();
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param param the parameter
   */
  protected abstract void addResultNode(ConnectorElement.Builder rootElement, T param);

  /**
   * gets all necessary data to create XML response.
   *
   * @param param the parameter
   * @param cmdContext command context
   * @return the warning code or null if it's correct.
   * {@link com.github.zhanhb.ckfinder.connector.api.ErrorCode} if no error
   * occurred.
   * @throws ConnectorException when error occurs
   */
  protected abstract ErrorListResult applyData(T param, CommandContext cmdContext) throws ConnectorException;

}
