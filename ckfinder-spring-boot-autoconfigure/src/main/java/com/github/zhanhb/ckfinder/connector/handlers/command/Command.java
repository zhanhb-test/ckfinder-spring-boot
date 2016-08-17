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

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.Arguments;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Base class for all command handlers.
 *
 * @param <T> arguments type
 */
@RequiredArgsConstructor
public abstract class Command<T extends Arguments> {

  /**
   * Connector configuration.
   */
  @Getter(AccessLevel.PROTECTED)
  private IConfiguration configuration;
  @Getter
  @NonNull
  private final Supplier<? extends T> argumentsSupplier;

  /**
   * Runs command. Initialize, sets response and execute command.
   *
   * @param request request
   * @param response response
   * @param configuration connector configuration
   * @throws ConnectorException when error occurred.
   */
  @SuppressWarnings("FinalMethod")
  public final void runCommand(HttpServletRequest request,
          HttpServletResponse response, IConfiguration configuration)
          throws ConnectorException {
    T arguments = argumentsSupplier.get();
    runWithArguments(request, response, configuration, arguments);
  }

  @Deprecated
  @SuppressWarnings("FinalMethod")
  public final void runWithArguments(HttpServletRequest request,
          HttpServletResponse response, IConfiguration configuration,
          T arguments) throws ConnectorException {
    this.initParams(arguments, request, configuration);
    try {
      setResponseHeader(request, response, arguments);
      execute(arguments, response);
    } catch (IOException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }
  }

  /**
   * initialize params for command handler.
   *
   * @param arguments
   * @param request request
   * @param configuration connector configuration
   * @throws ConnectorException to handle in error handler.
   */
  protected void initParams(T arguments, HttpServletRequest request,
          IConfiguration configuration) throws ConnectorException {
    this.configuration = configuration;
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(configuration.getUserRoleName());
    arguments.setUserRole(userRole);

    setCurrentFolderParam(request, arguments);

    String currentFolder = arguments.getCurrentFolder();
    checkConnectorEnabled();
    checkRequestPathValid(currentFolder);

    currentFolder = PathUtils.escape(currentFolder);
    arguments.setCurrentFolder(currentFolder);
    checkCurrentDirectoryHidden(arguments.getCurrentFolder());

    if (currentFolder == null || currentFolder.isEmpty()
            || isCurrFolderExists(arguments, request)) {
      arguments.setType(request.getParameter("type"));
    }
  }

  /**
   * check if connector is enabled and checks authentication.
   *
   * @return true if connector is enabled and user is authenticated
   * @throws ConnectorException when connector is disabled
   */
  private void checkConnectorEnabled() throws ConnectorException {
    if (!getConfiguration().isEnabled()) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_CONNECTOR_DISABLED, false);
    }
  }

  /**
   * Checks if current folder exists.
   *
   * @param arguments
   * @param request current request object
   * @return {@code true} if current folder exists
   * @throws ConnectorException if current folder doesn't exist
   */
  @Deprecated
  protected boolean isCurrFolderExists(T arguments, HttpServletRequest request)
          throws ConnectorException {
    String tmpType = request.getParameter("type");
    if (tmpType != null) {
      if (isTypeExists(arguments, tmpType)) {
        Path currDir = Paths.get(getConfiguration().getTypes().get(tmpType).getPath(),
                arguments.getCurrentFolder());
        if (!Files.isDirectory(currDir)) {
          throw new ConnectorException(
                  Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND,
                  false);
        } else {
          return true;
        }
      }
      return false;
    }
    return true;
  }

  /**
   * Checks if type of resource provided as parameter exists.
   *
   * @param arguments
   * @param type name of the resource type to check if it exists
   * @return {@code true} if provided type exists, {@code false} otherwise.
   */
  @Deprecated
  protected boolean isTypeExists(T arguments, String type) {
    ResourceType testType = getConfiguration().getTypes().get(type);
    return testType != null;
  }

  /**
   * checks if current folder is hidden.
   *
   * @param currentDirectory
   * @return false if isn't.
   * @throws ConnectorException when is hidden
   */
  private void checkCurrentDirectoryHidden(String currentDirectory) throws ConnectorException {
    if (FileUtils.isDirectoryHidden(currentDirectory, getConfiguration())) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST,
              false);
    }
  }

  /**
   * executes command and writes to response.
   *
   * @param response
   * @throws ConnectorException when error occurs
   * @throws java.io.IOException
   */
  abstract void execute(T arguments, HttpServletResponse response) throws ConnectorException, IOException;

  /**
   * sets header in response.
   *
   * @param request servlet request
   * @param response servlet response
   * @param arguments
   */
  abstract void setResponseHeader(HttpServletRequest request, HttpServletResponse response, T arguments);

  /**
   * check request for security issue.
   *
   * @param reqParam request param
   * @param arguments
   * @return true if validation passed
   * @throws ConnectorException if validation error occurs.
   */
  @Deprecated
  final void checkRequestPathValid(String reqParam) throws ConnectorException {
    if (reqParam == null || reqParam.isEmpty()) {
      return;
    }
    if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(reqParam).find()) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME,
              false);
    }
  }

  /**
   * gets current folder request param or sets default value if it's not set.
   *
   * @param request request
   * @param arguments
   */
  @Deprecated
  void setCurrentFolderParam(HttpServletRequest request, T arguments) {
    String currFolder = request.getParameter("currentFolder");
    if (currFolder == null || currFolder.isEmpty()) {
      arguments.setCurrentFolder("/");
    } else {
      arguments.setCurrentFolder(PathUtils.addSlashToBeginning(PathUtils.addSlashToEnd(currFolder)));
    }
  }

}
