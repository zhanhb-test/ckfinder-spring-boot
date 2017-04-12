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

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;

/**
 * Base class to handle XML commands.
 *
 * @param <T> parameter type
 */
public abstract class BaseXmlCommand<T extends Parameter> extends XmlCommand<T> {

  @Override
  @SuppressWarnings("FinalMethod")
  final Connector buildConnector(T param, CKFinderContext context)
          throws ConnectorException {
    Connector.Builder connector = Connector.builder();
    if (param.getType() != null) {
      connector.resourceType(param.getType().getName());
    }
    createCurrentFolderNode(param, connector, context.getAccessControl());
    createErrorNode(connector, 0);
    createXml(connector, param, context);
    return connector.build();
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param param the parameter
   * @param context ckfinder context
   * @throws ConnectorException when error occurs
   */
  protected abstract void createXml(Connector.Builder rootElement, T param, CKFinderContext context) throws ConnectorException;

}
