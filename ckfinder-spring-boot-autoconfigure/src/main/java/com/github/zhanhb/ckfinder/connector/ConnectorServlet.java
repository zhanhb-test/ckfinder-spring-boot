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

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.Command;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ExceptionHandler;
import com.github.zhanhb.ckfinder.connector.handlers.command.FileUploadCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.IPostCommand;
import com.github.zhanhb.ckfinder.connector.handlers.command.XmlCommand;
import com.github.zhanhb.ckfinder.connector.support.FallbackExceptionHandler;
import com.github.zhanhb.ckfinder.connector.support.XmlExceptionHandler;
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

  private final CKFinderContext context;

  public ConnectorServlet(CKFinderContext context) {
    this.context = Objects.requireNonNull(context);
  }

  /**
   * Handling get requests.
   *
   * @param request request
   * @param response response
   * @throws IOException when IO Exception occurs.
   * @throws ServletException when error occurs.
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
   * @throws IOException when IO Exception occurs.
   * @throws ServletException when error occurs.
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
   * @throws IOException when IO Exception occurs.
   */
  private void processRequest(HttpServletRequest request,
          HttpServletResponse response, boolean post)
          throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    String commandName = request.getParameter("command");
    ExceptionHandler handler = XmlExceptionHandler.INSTANCE;

    try {
      if (commandName == null || commandName.isEmpty()) {
        throw new ConnectorException(ErrorCode.INVALID_COMMAND);
      }

      Command command = context.getCommand(commandName);
      if (command == null) {
        throw new ConnectorException(ErrorCode.INVALID_COMMAND);
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
        throw new ConnectorException(ErrorCode.INVALID_REQUEST);
      }
      if (post && !FileUploadCommand.class.isAssignableFrom(commandClass)
              && !"true".equals(request.getParameter("CKFinderCommand"))) {
        throw new ConnectorException(ErrorCode.INVALID_REQUEST);
      }
      command.runCommand(request, response, context);
    } catch (SecurityException ex) {
      log.info("security exception", ex);
      handleException(new ConnectorException(ErrorCode.ACCESS_DENIED),
              context, request, response, handler);
    } catch (RuntimeException e) {
      log.error("runtime exception", e);
      handleException(new ConnectorException(ErrorCode.INVALID_COMMAND),
              context, request, response, handler);
    } catch (ConnectorException e) {
      log.debug("ConnectorException: {} {}", e.getErrorCode(), e.getMessage());
      handleException(e, context, request, response, handler);
    }
  }

  /**
   * handles error from execute command.
   *
   * @param e exception
   * @param request request
   * @param response response
   * @param context ckfinder context
   * @param handler exception handler
   * @throws IOException when IO Exception occurs.
   */
  private void handleException(ConnectorException e, CKFinderContext context,
          HttpServletRequest request, HttpServletResponse response,
          ExceptionHandler handler) throws IOException {
    handler.handleException(request, response, context, e);
  }

}
