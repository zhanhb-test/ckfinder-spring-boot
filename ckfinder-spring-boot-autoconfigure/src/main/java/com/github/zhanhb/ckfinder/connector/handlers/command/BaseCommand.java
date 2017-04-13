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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
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
public abstract class BaseCommand<T extends Parameter> implements Command {

  @SuppressWarnings("FinalMethod")
  @Override
  public final void runCommand(HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context)
          throws ConnectorException, IOException {
    execute(popupParams(request, context), request, response, context);
  }

  protected abstract T popupParams(HttpServletRequest request, CKFinderContext context) throws ConnectorException;

  /**
   * initialize params for command handler.
   *
   * @param <P> parameter type
   * @param param the parameter
   * @param request request
   * @param context ckfinder context
   * @return the parameter
   * @throws ConnectorException to handle in error handler.
   */
  @SuppressWarnings("FinalMethod")
  protected final <P extends Parameter> P doInitParam(P param, HttpServletRequest request,
          CKFinderContext context) throws ConnectorException {
    checkConnectorEnabled(context);
    setUserRole(param, request, context);
    String currentFolder = getCurrentFolder(request);
    param.setCurrentFolder(currentFolder);

    checkRequestPathValid(currentFolder);

    if (context.isDirectoryHidden(currentFolder)) {
      throw new ConnectorException(ErrorCode.INVALID_REQUEST);
    }

    String typeName = request.getParameter("type");
    ResourceType type = context.getTypes().get(typeName);
    if (currentFolder != null && typeName != null && type != null) {
      Path currDir = getPath(type.getPath(), currentFolder);
      if (!Files.isDirectory(currDir)) {
        throw new ConnectorException(ErrorCode.FOLDER_NOT_FOUND);
      }
    }
    param.setType(type);
    return param;
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

  /**
   * check request for security issue.
   *
   * @param reqParam request param
   * @throws ConnectorException if validation error occurs.
   */
  @SuppressWarnings("FinalMethod")
  final void checkRequestPathValid(String reqParam) throws ConnectorException {
    if (StringUtils.hasLength(reqParam) && Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(reqParam).find()) {
      throw new ConnectorException(ErrorCode.INVALID_NAME);
    }
  }

  private void setUserRole(Parameter param, HttpServletRequest request, CKFinderContext context) {
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(context.getUserRoleName());
    param.setUserRole(userRole);
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

}
