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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.Arguments;
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
public abstract class BaseXmlCommand<T extends Arguments> extends XmlCommand<T> {

  public BaseXmlCommand(Supplier<T> argumentsSupplier) {
    super(argumentsSupplier);
  }

  @Override
  @SuppressWarnings("FinalMethod")
  final Connector buildConnector(T arguments, IConfiguration configuration)
          throws ConnectorException {
    Connector.Builder connector = Connector.builder();
    createXml(arguments, configuration);
    if (arguments.getType() != null) {
      connector.resourceType(arguments.getType().getName());
    }
    createCurrentFolderNode(arguments, connector, configuration.getAccessControl());
    createErrorNode(connector, arguments);
    createXMLChildNodes(connector, arguments, configuration);
    return connector.build();
  }

  protected void createErrorNode(Connector.Builder rootElement, T arguments) {
    rootElement.error(Error.builder().number(0).build());
  }

  /**
   * abstract method to create XML nodes for commands.
   *
   * @param rootElement XML root node
   * @param arguments
   * @param configuration connector configuration
   */
  protected abstract void createXMLChildNodes(Connector.Builder rootElement, T arguments, IConfiguration configuration);

  /**
   * gets all necessary data to create XML response.
   *
   * @param arguments
   * @param configuration connector configuration
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  protected abstract void createXml(T arguments, IConfiguration configuration) throws ConnectorException;

  /**
   * creates <code>CurrentFolder</code> element.
   *
   * @param arguments
   * @param rootElement XML root node.
   * @param accessControl
   */
  protected void createCurrentFolderNode(T arguments, Connector.Builder rootElement, AccessControl accessControl) {
    if (arguments.getType() != null && arguments.getCurrentFolder() != null) {
      rootElement.currentFolder(CurrentFolder.builder()
              .path(arguments.getCurrentFolder())
              .url(arguments.getType().getUrl()
                      + arguments.getCurrentFolder())
              .acl(accessControl.getAcl(arguments.getType().getName(), arguments.getCurrentFolder(), arguments.getUserRole()))
              .build());
    }
  }

}
