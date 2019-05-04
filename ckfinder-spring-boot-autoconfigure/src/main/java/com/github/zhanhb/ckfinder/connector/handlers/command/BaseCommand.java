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
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.util.StringUtils;

/**
 * Base class for all command handlers.
 *
 * @param <T> parameter type
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
    if (StringUtils.hasLength(path) && Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(path).find()) {
      throw new ConnectorException(ErrorCode.INVALID_NAME);
    }
    return path;
  }

  @SuppressWarnings("FinalMethod")
  @Override
  public final void runCommand(HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException {
    execute(popupParams(request, context), request, response, context);
  }

  protected abstract T popupParams(HttpServletRequest request, CKFinderContext context);

  @SuppressWarnings("FinalMethod")
  protected final CommandContext populateCommandContext(HttpServletRequest request,
          CKFinderContext context) throws ConnectorException {
    checkConnectorEnabled(context);
    String userRole = getUserRole(request, context);
    String currentFolder = checkRequestPath(getCurrentFolder(request));

    if (context.isDirectoryHidden(currentFolder)) {
      throw new ConnectorException(ErrorCode.INVALID_REQUEST);
    }

    String typeName = request.getParameter("type");
    ResourceType type = context.getResource(typeName);
    if (currentFolder != null && typeName != null && type != null) {
      Path currDir = getPath(type.getPath(), currentFolder);
      if (!Files.isDirectory(currDir)) {
        throw new ConnectorException(ErrorCode.FOLDER_NOT_FOUND);
      }
    }
    return CommandContext.builder().cfCtx(context).userRole(userRole)
            .currentFolder(currentFolder).type(type).build();
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
    if (StringUtils.hasLength(currentFolder)) {
      return PathUtils.normalize('/' + currentFolder + '/');
    } else {
      return "/";
    }
  }

  @SuppressWarnings("FinalMethod")
  protected final Path getPath(Path first, String... more) {
    return first.getFileSystem().getPath(first.toString(), more);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
