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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import java.util.function.Supplier;

/**
 * Base class to handle XML commands.
 *
 * @param <T>
 */
public abstract class BaseXmlCommand<T extends Parameter> extends XmlCommand<T> {

  public BaseXmlCommand(Supplier<T> paramFactory) {
    super(paramFactory);
  }

  @Override
  @SuppressWarnings("FinalMethod")
  final Connector buildConnector(T param, IConfiguration configuration)
          throws ConnectorException {
    Connector.Builder connector = Connector.builder();
    createXml(param, configuration);
    if (param.getType() != null) {
      connector.resourceType(param.getType().getName());
    }
    createCurrentFolderNode(param, connector, configuration.getAccessControl());
    createErrorNode(connector, param);
    createXMLChildNodes(connector, param, configuration);
    return connector.build();
  }

  protected void createErrorNode(Connector.Builder rootElement, T param) {
    rootElement.error(Error.builder().number(0).build());
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param param
   * @param configuration connector configuration
   */
  protected abstract void createXMLChildNodes(Connector.Builder rootElement, T param, IConfiguration configuration);

  /**
   * gets all necessary data to create XML response.
   *
   * @param param
   * @param configuration connector configuration
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  protected abstract void createXml(T param, IConfiguration configuration) throws ConnectorException;

  /**
   * creates <code>CurrentFolder</code> element.
   *
   * @param param
   * @param rootElement XML root node.
   * @param accessControl
   */
  protected void createCurrentFolderNode(T param, Connector.Builder rootElement, AccessControl accessControl) {
    if (param.getType() != null && param.getCurrentFolder() != null) {
      rootElement.currentFolder(CurrentFolder.builder()
              .path(param.getCurrentFolder())
              .url(param.getType().getUrl()
                      + param.getCurrentFolder())
              .acl(accessControl.getAcl(param.getType().getName(), param.getCurrentFolder(), param.getUserRole()))
              .build());
    }
  }

}
