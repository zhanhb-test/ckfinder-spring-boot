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
import com.github.zhanhb.ckfinder.connector.data.FilePostParam;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.CopyFilesArguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CopyFiles;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.XMLCreator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CopyFiles</code> command.
 */
@Slf4j
public class CopyFilesCommand extends XMLCommand<CopyFilesArguments> implements IPostCommand {

  public CopyFilesCommand() {
    super(CopyFilesArguments::new);
  }

  @Override
  protected void createXMLChildNodes(int errorNum, Connector.Builder rootElement, CopyFilesArguments arguments, IConfiguration configuration) {
    XMLCreator.INSTANCE.addErrors(arguments, rootElement);

    if (arguments.isAddCopyNode()) {
      createCopyFielsNode(rootElement, arguments);
    }
  }

  /**
   * creates copy file XML node.
   *
   * @param rootElement XML root node.
   */
  private void createCopyFielsNode(Connector.Builder rootElement, CopyFilesArguments arguments) {
    rootElement.copyFiles(CopyFiles.builder()
            .copied(arguments.getFilesCopied())
            .copiedTotal(arguments.getCopiedAll() + arguments.getFilesCopied())
            .build());
  }

  @Override
  protected int getDataForXml(CopyFilesArguments arguments, IConfiguration configuration) {
    ResourceType type = arguments.getType();
    if (type == null) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE;
    }

    if (!configuration.getAccessControl().hasPermission(type.getName(),
            arguments.getCurrentFolder(),
            arguments.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_RENAME
            | AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE
            | AccessControl.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED;
    }

    try {
      return copyFiles(arguments, configuration, type);
    } catch (Exception e) {
      log.error("", e);
    }
    //this code should never be reached
    return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNKNOWN;
  }

  /**
   * copy files from request.
   *
   * @return error code
   */
  private int copyFiles(CopyFilesArguments arguments, IConfiguration configuration, ResourceType type) {
    arguments.setFilesCopied(0);
    arguments.setAddCopyNode(false);
    for (FilePostParam file : arguments.getFiles()) {

      if (!FileUtils.isFileNameInvalid(file.getName())) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }

      if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
              file.getFolder()).find()) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }
      if (configuration.getTypes().get(file.getType()) == null) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }
      if (file.getFolder() == null || file.getFolder().isEmpty()) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }
      if (!FileUtils.isFileExtensionAllwed(file.getName(),
              type)) {
        XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION,
                file.getName(), file.getFolder(), file.getType());
        continue;
      }
      // check #4 (extension) - when moving to another resource type,
      //double check extension
      if (!type.getName().equals(file.getType())) {
        if (!FileUtils.isFileExtensionAllwed(file.getName(),
                configuration.getTypes().get(file.getType()))) {
          XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION,
                  file.getName(), file.getFolder(), file.getType());
          continue;
        }
      }
      if (FileUtils.isDirectoryHidden(file.getFolder(), configuration)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }

      if (FileUtils.isFileHidden(file.getName(), configuration)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }

      if (!configuration.getAccessControl().hasPermission(file.getType(), file.getFolder(), arguments.getUserRole(),
              AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED;
      }

      Path sourceFile = Paths.get(configuration.getTypes().get(file.getType()).getPath(),
              file.getFolder(), file.getName());
      Path destFile = Paths.get(type.getPath(),
              arguments.getCurrentFolder(), file.getName());

      try {
        if (!Files.isRegularFile(sourceFile)) {
          XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND,
                  file.getName(), file.getFolder(), file.getType());
          continue;
        }
        if (!type.getName().equals(file.getType())) {
          long maxSize = type.getMaxSize();
          if (maxSize != 0 && maxSize < Files.size(sourceFile)) {
            XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG,
                    file.getName(), file.getFolder(), file.getType());
            continue;
          }
        }
        if (sourceFile.equals(destFile)) {
          XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_SOURCE_AND_TARGET_PATH_EQUAL,
                  file.getName(), file.getFolder(), file.getType());
        } else if (Files.exists(destFile)) {
          if (file.getOptions() != null
                  && file.getOptions().contains("overwrite")) {
            if (!handleOverwrite(sourceFile, destFile)) {
              XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                      file.getName(), file.getFolder(), file.getType());
            } else {
              arguments.filesCopiedPlus();
            }
          } else if (file.getOptions() != null && file.getOptions().contains("autorename")) {
            if (!handleAutoRename(sourceFile, destFile)) {
              XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                      file.getName(), file.getFolder(), file.getType());
            } else {
              arguments.filesCopiedPlus();
            }
          } else {
            XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST,
                    file.getName(), file.getFolder(), file.getType());
          }
        } else if (FileUtils.copyFromSourceToDestFile(sourceFile, destFile,
                false)) {
          arguments.filesCopiedPlus();
          copyThumb(file, arguments, configuration);
        }
      } catch (SecurityException | IOException e) {
        log.error("", e);
        XMLCreator.INSTANCE.appendErrorNodeChild(arguments, Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                file.getName(), file.getFolder(), file.getType());
      }
    }
    arguments.setAddCopyNode(true);
    if (XMLCreator.INSTANCE.hasErrors(arguments)) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_COPY_FAILED;
    } else {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE;
    }
  }

  /**
   * Handles autorename option.
   *
   * @param sourceFile source file to copy from.
   * @param destFile destination file to copy to.
   * @return true if copied correctly
   * @throws IOException when ioerror occurs
   */
  private boolean handleAutoRename(Path sourceFile, Path destFile)
          throws IOException {
    String fileName = destFile.getFileName().toString();
    String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName, false);
    String fileExtension = FileUtils.getFileExtension(fileName, false);
    for (int counter = 1;; counter++) {
      String newFileName = fileNameWithoutExtension
              + "(" + counter + ")."
              + fileExtension;
      Path newDestFile = destFile.resolveSibling(newFileName);
      if (!Files.exists(newDestFile)) {
        // can't be in one if=, because when error in
        // copy file occurs then it will be infinity loop
        log.debug("prepare copy file '{}' to '{}'", sourceFile, newDestFile);
        return (FileUtils.copyFromSourceToDestFile(sourceFile,
                newDestFile, false));
      }
    }
  }

  /**
   * Handles overwrite option.
   *
   * @param sourceFile source file to copy from.
   * @param destFile destination file to copy to.
   * @return true if copied correctly
   * @throws IOException when ioerror occurs
   */
  private boolean handleOverwrite(Path sourceFile, Path destFile) throws IOException {
    return FileUtils.delete(destFile)
            && FileUtils.copyFromSourceToDestFile(sourceFile, destFile,
                    false);
  }

  /**
   * copy thumb file.
   *
   * @param file file to copy.
   * @throws IOException when ioerror occurs
   */
  private void copyThumb(FilePostParam file, CopyFilesArguments arguments, IConfiguration configuration) throws IOException {
    Path sourceThumbFile = Paths.get(configuration.getThumbsPath(),
            file.getType(), file.getFolder(), file.getName());
    Path destThumbFile = Paths.get(configuration.getThumbsPath(),
            arguments.getType().getName(), arguments.getCurrentFolder(),
            file.getName());

    log.debug("copy thumb from '{}' to '{}'", sourceThumbFile, destThumbFile);

    if (Files.isRegularFile(sourceThumbFile)) {
      FileUtils.copyFromSourceToDestFile(sourceThumbFile, destThumbFile,
              false);
    }
  }

  @Override
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  protected void initParams(CopyFilesArguments arguments, HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    super.initParams(arguments, request, configuration);
    arguments.setFiles(new ArrayList<>());
    arguments.setCopiedAll(request.getParameter("copied") != null ? Integer.parseInt(request.getParameter("copied")) : 0);

    RequestFileHelper.addFilesListFromRequest(request, arguments.getFiles());
  }

}
