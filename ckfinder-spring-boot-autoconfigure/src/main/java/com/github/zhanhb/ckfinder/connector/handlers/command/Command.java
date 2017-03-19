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

import com.github.zhanhb.ckfinder.connector.configuration.ConnectorError;
import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.configuration.ParameterFactory;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Base class for all command handlers.
 *
 * @param <T> parameter type
 */
public abstract class Command<T extends Parameter> {

  private final ParameterFactory<T> paramFactory;

  protected Command(ParameterFactory<T> paramFactory) {
    this.paramFactory = Objects.requireNonNull(paramFactory);
  }

  /**
   * Runs command. Initialize, sets response and execute command.
   *
   * @param request request
   * @param response response
   * @param configuration connector configuration
   * @throws ConnectorException when error occurred.
   * @throws java.io.IOException
   */
  @SuppressWarnings("FinalMethod")
  public final void runCommand(HttpServletRequest request,
          HttpServletResponse response, IConfiguration configuration)
          throws ConnectorException, IOException {
    T param = paramFactory.newParameter();
    initParams(param, request, configuration);

    execute(param, request, response, configuration);
  }

  /**
   * initialize params for command handler.
   *
   * @param param
   * @param request request
   * @param configuration connector configuration
   * @throws ConnectorException to handle in error handler.
   */
  protected void initParams(T param, HttpServletRequest request,
          IConfiguration configuration) throws ConnectorException {
    checkConnectorEnabled(configuration);
    setUserRole(param, request, configuration);
    String currentFolder = getCurrentFolder(request);
    param.setCurrentFolder(currentFolder);

    checkRequestPathValid(currentFolder);

    if (FileUtils.isDirectoryHidden(currentFolder, configuration)) {
      throw new ConnectorException(ConnectorError.INVALID_REQUEST);
    }

    String typeName = request.getParameter("type");
    ResourceType type = configuration.getTypes().get(typeName);
    if (currentFolder != null && typeName != null && type != null) {
      Path currDir = Paths.get(type.getPath(), currentFolder);
      if (!Files.isDirectory(currDir)) {
        throw new ConnectorException(ConnectorError.FOLDER_NOT_FOUND);
      }
    }
    param.setType(type);
  }

  /**
   * check if connector is enabled and checks authentication.
   *
   * @param configuration
   * @throws ConnectorException when connector is disabled
   */
  private void checkConnectorEnabled(IConfiguration configuration) throws ConnectorException {
    if (!configuration.isEnabled()) {
      throw new ConnectorException(ConnectorError.CONNECTOR_DISABLED);
    }
  }

  /**
   * executes command and writes to response.
   *
   * @param param
   * @param response
   * @param request
   * @param configuration
   * @throws ConnectorException when error occurs
   * @throws java.io.IOException
   */
  abstract void execute(T param, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration) throws ConnectorException, IOException;

  /**
   * check request for security issue.
   *
   * @param reqParam request param
   * @throws ConnectorException if validation error occurs.
   */
  @SuppressWarnings("FinalMethod")
  final void checkRequestPathValid(String reqParam) throws ConnectorException {
    if (reqParam != null && !reqParam.isEmpty()
            && Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(reqParam).find()) {
      throw new ConnectorException(ConnectorError.INVALID_NAME);
    }
  }

  private void setUserRole(T param, HttpServletRequest request, IConfiguration configuration) {
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(configuration.getUserRoleName());
    param.setUserRole(userRole);
  }

  /**
   * gets current folder request param or sets default value if it's not set.
   *
   * @param request request
   * @return
   */
  String getCurrentFolder(HttpServletRequest request) {
    String currentFolder = request.getParameter("currentFolder");
    if (currentFolder != null && !currentFolder.isEmpty()) {
      currentFolder = PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(currentFolder));
    } else {
      currentFolder = "/";
    }
    currentFolder = PathUtils.escape(currentFolder);
    return currentFolder;
  }

}
