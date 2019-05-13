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
import com.github.zhanhb.ckfinder.connector.api.Command;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.util.StringUtils;

/**
 * Base class for all command handlers.
 *
 * @param <T> parameter resource
 */
public abstract class BaseCommand<T> implements Command {

  /**
   * check request for security issue.
   *
   * @param path request path
   * @throws ConnectorException if validation error occurs.
   */
  @SuppressWarnings("FinalMethod")
  static String checkRequestPath(String path) throws ConnectorException {
    if (StringUtils.hasLength(path) && FileUtils.isPathNameInvalid(path)) {
      throw new ConnectorException(ErrorCode.INVALID_NAME);
    }
    return path;
  }

  @SuppressWarnings("FinalMethod")
  @Override
  public final void runCommand(HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException {
    execute(parseParameters(request, context), request, response, context);
  }

  protected abstract T parseParameters(HttpServletRequest request, CKFinderContext context);

  private String nullToEmpty(String s) {
    return s != null ? s : "";
  }

  /**
   * Checks if the request contains a valid CSRF token that matches the value
   * sent in the cookie.<br>
   *
   * @see
   * <a href="https://www.owasp.org/index.php/Cross-Site_Request_Forgery_(CSRF)_Prevention_Cheat_Sheet#Double_Submit_Cookies">Cross-Site_Request_Forgery_(CSRF)_Prevention</a>
   *
   * @param request current request object parameter
   */
  private void checkCsrfToken(final HttpServletRequest request) throws ConnectorException {
    final String tokenParamName = "ckCsrfToken";
    final String tokenCookieName = "ckCsrfToken";
    final int minTokenLength = 32;
    final String paramToken = nullToEmpty(request.getParameter(tokenParamName)).trim();

    Cookie[] cookies = request.getCookies();
    String cookieToken = "";
    for (Cookie cookie : cookies) {
      if (cookie.getName().equals(tokenCookieName)) {
        cookieToken = nullToEmpty(cookie.getValue()).trim();
        break;
      }
    }
    if (paramToken.length() >= minTokenLength && cookieToken.length() >= minTokenLength
            && paramToken.equals(cookieToken)) {
      return;
    }
    throw new ConnectorException(ErrorCode.INVALID_REQUEST, "CSRF Attempt");
  }

  @SuppressWarnings("FinalMethod")
  final CommandContext populateCommandContext(HttpServletRequest request,
          CKFinderContext context) throws ConnectorException {
    if (context.isCsrfProtectionEnabled() && this instanceof IPostCommand) {
      checkCsrfToken(request);
    }
    checkConnectorEnabled(context);
    String userRole = getUserRole(request, context);
    String currentFolder = checkRequestPath(getCurrentFolder(request));

    ResourceType resource = context.getResource(request.getParameter("type"));
    return new CommandContext(context, userRole, currentFolder, resource);
  }

  /**
   * check if connector is enabled and checks authentication.
   *
   * @param context ckfinder context
   * @throws ConnectorException when connector is disabled
   */
  private void checkConnectorEnabled(CKFinderContext context) throws ConnectorException {
    if (!context.isEnabled()) {
      throw new ConnectorException(ErrorCode.CONNECTOR_DISABLED);
    }
  }

  /**
   * executes command and writes to response.
   *
   * @param param the parameter
   * @param request request
   * @param response response
   * @param context ckfinder context
   * @throws ConnectorException when error occurs
   * @throws IOException when IO Exception occurs.
   */
  abstract void execute(T param, HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException;

  private String getUserRole(HttpServletRequest request, CKFinderContext context) {
    HttpSession session = request.getSession(false);
    if (session == null) {
      return null;
    }
    return (String) session.getAttribute(context.getUserRoleName());
  }

  /**
   * gets current folder request parameter or sets default value if it's not
   * set.
   *
   * @param request request
   * @return current folder, / if empty
   */
  String getCurrentFolder(HttpServletRequest request) {
    String currentFolder = request.getParameter("currentFolder");
    return currentFolder != null ? PathUtils.normalize('/' + currentFolder + '/') : "/";
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
