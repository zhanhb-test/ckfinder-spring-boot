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
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.GetFoldersParameter;
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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>GetFolders</code> command. Get subfolders for selected
 * location command.
 */
@Slf4j
public class GetFoldersCommand extends BaseXmlCommand<GetFoldersParameter> {

  public GetFoldersCommand() {
    super(GetFoldersParameter::new);
  }

  @Override
  protected void createXml(Connector.Builder rootElement, GetFoldersParameter param, IConfiguration configuration) throws ConnectorException {
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_VIEW)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }
    if (FileUtils.isDirectoryHidden(param.getCurrentFolder(), configuration)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
    }

    Path dir = Paths.get(param.getType().getPath(),
            param.getCurrentFolder());
    try {
      if (!Files.exists(dir)) {
        param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND);
      }

      param.setDirectories(FileUtils.findChildrensList(dir, true));
    } catch (IOException | SecurityException e) {
      log.error("", e);
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
    }
    filterListByHiddenAndNotAllowed(param, configuration);
    createFoldersData(rootElement, param, configuration);
  }

  /**
   * filters list and check if every element is not hidden and have correct ACL.
   */
  private void filterListByHiddenAndNotAllowed(GetFoldersParameter param, IConfiguration configuration) {
    List<String> tmpDirs = param.getDirectories().stream()
            .filter(dir -> (configuration.getAccessControl().hasPermission(param.getType().getName(), param.getCurrentFolder() + dir, param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_VIEW)
            && !FileUtils.isDirectoryHidden(dir, configuration)))
            .sorted()
            .collect(Collectors.toList());

    param.getDirectories().clear();
    param.getDirectories().addAll(tmpDirs);

  }

  /**
   * creates folder data node in XML document.
   *
   * @param rootElement root element in XML document
   */
  private void createFoldersData(Connector.Builder rootElement, GetFoldersParameter param, IConfiguration configuration) {
    Folders.Builder folders = Folders.builder();
    for (String dirPath : param.getDirectories()) {
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
