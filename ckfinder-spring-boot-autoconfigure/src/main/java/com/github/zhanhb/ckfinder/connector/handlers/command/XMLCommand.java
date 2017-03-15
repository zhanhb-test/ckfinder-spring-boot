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
import com.github.zhanhb.ckfinder.connector.handlers.arguments.XMLArguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.XMLCreator;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class to handle XML commands.
 *
 * @param <T>
 */
public abstract class XMLCommand<T extends XMLArguments> extends Command<T> {

  public XMLCommand(Supplier<T> argumentsSupplier) {
    super(argumentsSupplier);
  }

  /**
   * executes XML command. Creates XML response and writes it to response output
   * stream.
   *
   * @param arguments
   * @param configuration
   * @throws java.io.IOException
   */
  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(T arguments, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration)
          throws IOException, ConnectorException {
    createXml(arguments, configuration);
    Connector.Builder rootElement = arguments.getConnector();
    if (arguments.getType() != null) {
      rootElement.resourceType(arguments.getType().getName());
    }
    createCurrentFolderNode(arguments, rootElement, configuration.getAccessControl());
    createErrorNode(rootElement, arguments);
    createXMLChildNodes(rootElement, arguments, configuration);

    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      XMLCreator.INSTANCE.writeTo(arguments.getConnector().build(), out);
    }
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
