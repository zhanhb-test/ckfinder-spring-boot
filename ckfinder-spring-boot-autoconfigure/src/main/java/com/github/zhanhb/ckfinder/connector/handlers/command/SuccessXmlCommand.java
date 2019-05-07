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
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;

/**
 * Base class to handle XML commands.
 *
 * @param <T> parameter type
 */
public abstract class SuccessXmlCommand<T> extends XmlCommand<T> {

  @Override
  @SuppressWarnings("FinalMethod")
  final Connector buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException {
    Connector.Builder connector = Connector.builder();
    cmdContext.setResourceType(connector);
    createCurrentFolderNode(cmdContext, connector);
    createErrorNode(connector, 0);
    createXml(connector, param, cmdContext);
    return connector.build();
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param param the parameter
   * @param cmdContext command context
   * @throws ConnectorException when error occurs
   */
  protected abstract void createXml(Connector.Builder rootElement, T param, CommandContext cmdContext) throws ConnectorException;

}
