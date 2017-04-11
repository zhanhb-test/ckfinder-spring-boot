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

import com.github.zhanhb.ckfinder.connector.api.Command;
import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
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
          HttpServletResponse response, Configuration configuration)
          throws ConnectorException, IOException {
    execute(popupParams(request, configuration), request, response, configuration);
  }

  protected abstract T popupParams(HttpServletRequest request, Configuration configuration) throws ConnectorException;

  /**
   * initialize params for command handler.
   *
   * @param <T> parameter type
   * @param param the parameter
   * @param request request
   * @param configuration connector configuration
   * @return the parameter
   * @throws ConnectorException to handle in error handler.
   */
  @SuppressWarnings("FinalMethod")
  protected final <T extends Parameter> T doInitParam(T param, HttpServletRequest request,
          Configuration configuration) throws ConnectorException {
    checkConnectorEnabled(configuration);
    setUserRole(param, request, configuration);
    String currentFolder = getCurrentFolder(request);
    param.setCurrentFolder(currentFolder);

    checkRequestPathValid(currentFolder);

    if (configuration.isDirectoryHidden(currentFolder)) {
      throw new ConnectorException(ConnectorError.INVALID_REQUEST);
    }

    String typeName = request.getParameter("type");
    ResourceType type = configuration.getTypes().get(typeName);
    if (currentFolder != null && typeName != null && type != null) {
      Path currDir = getPath(type.getPath(), currentFolder);
      if (!Files.isDirectory(currDir)) {
        throw new ConnectorException(ConnectorError.FOLDER_NOT_FOUND);
      }
    }
    param.setType(type);
    return param;
  }

  /**
   * check if connector is enabled and checks authentication.
   *
   * @param configuration connector configuration
   * @throws ConnectorException when connector is disabled
   */
  private void checkConnectorEnabled(Configuration configuration) throws ConnectorException {
    if (!configuration.isEnabled()) {
      throw new ConnectorException(ConnectorError.CONNECTOR_DISABLED);
    }
  }

  /**
   * executes command and writes to response.
   *
   * @param param the parameter
   * @param request request
   * @param response response
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs
   * @throws IOException when IO Exception occurs.
   */
  abstract void execute(T param, HttpServletRequest request,
          HttpServletResponse response, Configuration configuration)
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
      throw new ConnectorException(ConnectorError.INVALID_NAME);
    }
  }

  private void setUserRole(Parameter param, HttpServletRequest request, Configuration configuration) {
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(configuration.getUserRoleName());
    param.setUserRole(userRole);
  }

  /**
   * gets current folder request parameter or sets default value if it's not set.
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
