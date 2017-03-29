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
package com.github.zhanhb.ckfinder.connector;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.errors.ExceptionHandler;
import com.github.zhanhb.ckfinder.connector.errors.FallbackExceptionHandler;
import com.github.zhanhb.ckfinder.connector.errors.XmlExceptionHandler;
import com.github.zhanhb.ckfinder.connector.handlers.command.Command;
import com.github.zhanhb.ckfinder.connector.handlers.command.FileUploadCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.XmlCommand;
import java.io.IOException;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Main connector servlet for handling CKFinder requests.
 */
@Slf4j
public class ConnectorServlet extends HttpServlet {

  private static final long serialVersionUID = 2960665641425153638L;

  private final IConfiguration configuration;

  public ConnectorServlet(IConfiguration configuration) {
    this.configuration = Objects.requireNonNull(configuration);
  }

  /**
   * Handling get requests.
   *
   * @param request request
   * @param response response
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response, false);
  }

  /**
   * Handling post requests.
   *
   * @param request request
   * @param response response
   * @throws IOException .
   * @throws ServletException .
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response, true);
  }

  /**
   * Creating response for every command in request parameter.
   *
   * @param request request
   * @param response response
   * @param post if it's post command.
   * @throws ServletException when error occurs.
   * @throws java.io.IOException
   */
  private void processRequest(HttpServletRequest request,
          HttpServletResponse response, boolean post)
          throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    String commandName = request.getParameter("command");
    ExceptionHandler handler = XmlExceptionHandler.INSTANCE;

    try {
      if (commandName == null || commandName.isEmpty()) {
        throw new ConnectorException(ConnectorError.INVALID_COMMAND);
      }

      Command<?> command = configuration.getCommandFactory().getCommand(commandName);
      if (command == null) {
        throw new ConnectorException(ConnectorError.INVALID_COMMAND);
      }
      log.debug("command: {}", command);
      if (command instanceof ExceptionHandler) {
        handler = (ExceptionHandler) command;
      } else if (!(command instanceof XmlCommand)) {
        handler = FallbackExceptionHandler.INSTANCE;
      }
      // checks if command should go via POST request or it's a post request
      // and it's not upload command
      Class<?> commandClass = command.getClass();
      if (IPostCommand.class.isAssignableFrom(commandClass) != post) {
        throw new ConnectorException(ConnectorError.INVALID_REQUEST);
      }
      if (post && !FileUploadCommand.class.isAssignableFrom(commandClass)
              && !"true".equals(request.getParameter("CKFinderCommand"))) {
        throw new ConnectorException(ConnectorError.INVALID_REQUEST);
      }
      command.runCommand(request, response, configuration);
    } catch (SecurityException ex) {
      log.error("", ex);
      handleException(new ConnectorException(ConnectorError.ACCESS_DENIED),
              configuration, request, response, handler);
    } catch (RuntimeException e) {
      log.error("", e);
      handleException(new ConnectorException(ConnectorError.INVALID_COMMAND),
              configuration, request, response, handler);
    } catch (ConnectorException e) {
      log.error("", e);
      handleException(e, configuration, request, response, handler);
    }
  }

  /**
   * handles error from execute command.
   *
   * @param e exception
   * @param request request
   * @param response response
   * @param configuration connector configuration
   * @param handler exception handler
   * @throws java.io.IOException
   */
  private void handleException(ConnectorException e, IConfiguration configuration,
          HttpServletRequest request, HttpServletResponse response,
          ExceptionHandler handler) throws IOException {
    handler.handleException(request, response, configuration, e);
  }

}
