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
import com.github.zhanhb.ckfinder.connector.data.FilePostParam;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CopyFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CopyFiles;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CopyFiles</code> command.
 */
@Slf4j
public class CopyFilesCommand extends ErrorListXmlCommand<CopyFilesParameter> implements IPostCommand {

  public CopyFilesCommand() {
    super(CopyFilesParameter::new);
  }

  @Override
  protected void addRestNodes(Connector.Builder rootElement, CopyFilesParameter param, IConfiguration configuration) {
    if (param.isAddCopyNode()) {
      rootElement.copyFiles(CopyFiles.builder()
              .copied(param.getFilesCopied())
              .copiedTotal(param.getCopiedAll() + param.getFilesCopied())
              .build());
    }
  }

  @Override
  protected ConnectorError getDataForXml(CopyFilesParameter param, IConfiguration configuration)
          throws ConnectorException {
    ResourceType type = param.getType();
    if (type == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    assert type != null;
    if (!configuration.getAccessControl().hasPermission(type.getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.FILE_RENAME
            | AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    return copyFiles(param, configuration, type);
  }

  /**
   * copy files from request.
   *
   * @param param
   * @param type
   * @param configuration
   * @return error code
   */
  private ConnectorError copyFiles(CopyFilesParameter param, IConfiguration configuration, ResourceType type) {
    param.setFilesCopied(0);
    param.setAddCopyNode(false);
    for (FilePostParam file : param.getFiles()) {

      if (!FileUtils.isFileNameValid(file.getName())) {
        return ConnectorError.INVALID_REQUEST;
      }

      if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
              file.getFolder()).find()) {
        return ConnectorError.INVALID_REQUEST;
      }
      if (file.getType() == null) {
        return ConnectorError.INVALID_REQUEST;
      }
      if (file.getFolder() == null || file.getFolder().isEmpty()) {
        return ConnectorError.INVALID_REQUEST;
      }
      if (!FileUtils.isFileExtensionAllowed(file.getName(), type)) {
        param.appendErrorNodeChild(ConnectorError.INVALID_EXTENSION,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }
      // check #4 (extension) - when moving to another resource type,
      //double check extension
      if (type != file.getType()) {
        if (!FileUtils.isFileExtensionAllowed(file.getName(), file.getType())) {
          param.appendErrorNodeChild(ConnectorError.INVALID_EXTENSION,
                  file.getName(), file.getFolder(), file.getType().getName());
          continue;
        }
      }
      if (FileUtils.isDirectoryHidden(file.getFolder(), configuration)) {
        return ConnectorError.INVALID_REQUEST;
      }

      if (FileUtils.isFileHidden(file.getName(), configuration)) {
        return ConnectorError.INVALID_REQUEST;
      }

      if (!configuration.getAccessControl().hasPermission(file.getType().getName(), file.getFolder(), param.getUserRole(),
              AccessControl.FILE_VIEW)) {
        return ConnectorError.UNAUTHORIZED;
      }

      Path sourceFile = Paths.get(file.getType().getPath(),
              file.getFolder(), file.getName());
      Path destFile = Paths.get(type.getPath(),
              param.getCurrentFolder(), file.getName());

      try {
        if (!Files.isRegularFile(sourceFile)) {
          param.appendErrorNodeChild(ConnectorError.FILE_NOT_FOUND,
                  file.getName(), file.getFolder(), file.getType().getName());
          continue;
        }
        if (type != file.getType()) {
          long maxSize = type.getMaxSize();
          if (maxSize != 0 && maxSize < Files.size(sourceFile)) {
            param.appendErrorNodeChild(ConnectorError.UPLOADED_TOO_BIG,
                    file.getName(), file.getFolder(), file.getType().getName());
            continue;
          }
        }
        if (sourceFile.equals(destFile)) {
          param.appendErrorNodeChild(ConnectorError.SOURCE_AND_TARGET_PATH_EQUAL,
                  file.getName(), file.getFolder(), file.getType().getName());
        } else if (Files.exists(destFile)) {
          if (file.getOptions() != null
                  && file.getOptions().contains("overwrite")) {
            if (!handleOverwrite(sourceFile, destFile)) {
              param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                      file.getName(), file.getFolder(), file.getType().getName());
            } else {
              param.filesCopiedPlus();
            }
          } else if (file.getOptions() != null && file.getOptions().contains("autorename")) {
            if (!handleAutoRename(sourceFile, destFile)) {
              param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                      file.getName(), file.getFolder(), file.getType().getName());
            } else {
              param.filesCopiedPlus();
            }
          } else {
            param.appendErrorNodeChild(ConnectorError.ALREADY_EXIST,
                    file.getName(), file.getFolder(), file.getType().getName());
          }
        } else if (FileUtils.copyFromSourceToDestFile(sourceFile, destFile, false)) {
          param.filesCopiedPlus();
          copyThumb(file, param, configuration);
        }
      } catch (SecurityException | IOException e) {
        log.error("", e);
        param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                file.getName(), file.getFolder(), file.getType().getName());
      }
    }
    param.setAddCopyNode(true);
    if (param.hasError()) {
      return ConnectorError.COPY_FAILED;
    } else {
      return null;
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
        return FileUtils.copyFromSourceToDestFile(sourceFile,
                newDestFile, false);
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
    return FileUtils.copyFromSourceToDestFile(sourceFile, destFile, false);
  }

  /**
   * copy thumb file.
   *
   * @param file file to copy.
   * @param param
   * @param configuration
   * @throws IOException when ioerror occurs
   */
  private void copyThumb(FilePostParam file, CopyFilesParameter param, IConfiguration configuration) throws IOException {
    Path sourceThumbFile = Paths.get(configuration.getThumbsPath(),
            file.getType().getName(), file.getFolder(), file.getName());
    Path destThumbFile = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder(),
            file.getName());

    log.debug("copy thumb from '{}' to '{}'", sourceThumbFile, destThumbFile);

    if (Files.isRegularFile(sourceThumbFile)) {
      FileUtils.copyFromSourceToDestFile(sourceThumbFile, destThumbFile,
              false);
    }
  }

  @Override
  protected void initParams(CopyFilesParameter param, HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setCopiedAll(request.getParameter("copied") != null ? Integer.parseInt(request.getParameter("copied")) : 0);

    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), configuration);
  }

}
