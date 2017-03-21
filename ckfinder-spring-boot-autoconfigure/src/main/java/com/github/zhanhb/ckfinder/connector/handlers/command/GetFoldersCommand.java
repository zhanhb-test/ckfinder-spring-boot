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
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.Folder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Folders;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>GetFolders</code> command. Get subfolders for selected
 * location command.
 */
@Slf4j
public class GetFoldersCommand extends BaseXmlCommand<Parameter> {

  public GetFoldersCommand() {
    super(Parameter::new);
  }

  @Override
  protected void createXml(Connector.Builder rootElement, Parameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FOLDER_VIEW)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }
    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }

    Path dir = Paths.get(param.getType().getPath(),
            param.getCurrentFolder());
    try {
      if (!Files.isDirectory(dir)) {
        param.throwException(ConnectorError.FOLDER_NOT_FOUND);
      }

      List<String> directories = FileUtils.findChildrensList(dir, true);
      filterListByHiddenAndNotAllowed(directories, param, configuration);
      createFoldersData(rootElement, param, configuration, directories);
    } catch (IOException | SecurityException e) {
      log.error("", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  /**
   * filters list and check if every element is not hidden and have correct ACL.
   *
   * @param directories
   * @param configuration
   * @param param
   */
  private void filterListByHiddenAndNotAllowed(List<String> directories, Parameter param, IConfiguration configuration) {
    directories.removeIf(dir -> !configuration.getAccessControl().hasPermission(param.getType().getName(), param.getCurrentFolder() + dir, param.getUserRole(),
            AccessControl.FOLDER_VIEW)
            || configuration.isDirectoryHidden(dir));
  }

  /**
   * creates folder data node in XML document.
   *
   * @param rootElement root element in XML document
   * @param param
   * @param directories
   * @param configuration
   */
  private void createFoldersData(Connector.Builder rootElement, Parameter param, IConfiguration configuration, List<String> directories) {
    Folders.Builder folders = Folders.builder();
    for (String dirPath : directories) {
      Path dir = Paths.get(param.getType().getPath(),
              param.getCurrentFolder(), dirPath);
      if (Files.exists(dir)) {
        boolean hasChildren = FileUtils.hasChildren(configuration.getAccessControl(),
                param.getCurrentFolder() + dirPath + "/", dir,
                configuration, param.getType().getName(), param.getUserRole());
        folders.folder(Folder.builder()
                .name(dirPath)
                .hasChildren(hasChildren)
                .acl(configuration.getAccessControl()
                        .getAcl(param.getType().getName(),
                                param.getCurrentFolder()
                                + dirPath, param.getUserRole())).build());
      }
    }
    rootElement.folders(folders.build());
  }

}
