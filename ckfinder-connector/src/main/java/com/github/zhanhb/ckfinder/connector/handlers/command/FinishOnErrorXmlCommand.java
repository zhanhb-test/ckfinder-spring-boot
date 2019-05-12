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
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorCodeElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;

/**
 * Base class to handle XML commands.
 *
 * @param <T> parameter type
 */
public abstract class FinishOnErrorXmlCommand<T> extends XmlCommand<T> {

  @Override
  @SuppressWarnings("FinalMethod")
  final ConnectorElement buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException {
    ConnectorElement.Builder connector = ConnectorElement.builder();
    cmdContext.setResourceType(connector);
    createCurrentFolderNode(cmdContext, connector);
    connector.error(ErrorCodeElement.builder().number(0).build());
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
  protected abstract void createXml(ConnectorElement.Builder rootElement, T param, CommandContext cmdContext) throws ConnectorException;

}
