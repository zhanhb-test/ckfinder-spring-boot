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
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;

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
    createCurrentFolderNode(cmdContext, connector);
    createXml(param, cmdContext, connector);
    return connector.build();
  }

  /**
   * gets all necessary data to create XML response.
   *
   * @param param the parameter
   * @param cmdContext command context
   * @param builder XML root node
   * @return the warning code or null if it's correct.
   * {@link com.github.zhanhb.ckfinder.connector.api.ErrorCode} if no error
   * occurred.
   * @throws ConnectorException when error occurs
   */
  protected abstract void createXml(T param, CommandContext cmdContext,
          ConnectorElement.Builder builder) throws ConnectorException;

}
