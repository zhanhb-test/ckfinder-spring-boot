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
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.MoveFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.MoveFiles;
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
 * Class to handle <code>MoveFiles</code> command.
 */
@Slf4j
public class MoveFilesCommand extends ErrorListXmlCommand<MoveFilesParameter> implements IPostCommand {

  public MoveFilesCommand() {
    super(MoveFilesParameter::new);
  }

  @Override
  protected void createXMLChildNodes(Connector.Builder rootElement, MoveFilesParameter param, IConfiguration configuration) {
    param.addErrorsTo(rootElement);

    if (param.isAddMoveNode()) {
      createMoveFielsNode(rootElement, param);
    }
  }

  /**
   * creates move file XML node.
   *
   * @param rootElement XML root element.
   */
  private void createMoveFielsNode(Connector.Builder rootElement, MoveFilesParameter param) {
    rootElement.moveFiles(MoveFiles.builder()
            .moved(param.getFilesMoved())
            .movedTotal(param.getMovedAll() + param.getFilesMoved())
            .build());
  }

  @Override
  protected int getDataForXml(MoveFilesParameter param, IConfiguration configuration)
          throws ConnectorException {
    if (param.getType() == null) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_RENAME
            | AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE
            | AccessControl.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD)) {
      param.throwException(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
    }

    try {
      return moveFiles(param, configuration);
    } catch (Exception e) {
      log.error("", e);
    }
    //this code should never be reached
    return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNKNOWN;

  }

  /**
   * move files.
   *
   * @return error code.
   */
  private int moveFiles(MoveFilesParameter param, IConfiguration configuration) {
    param.setFilesMoved(0);
    param.setAddMoveNode(false);
    for (FilePostParam file : param.getFiles()) {

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
      if (!FileUtils.isFileExtensionAllwed(file.getName(), param.getType())) {
        param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION,
                file.getName(), file.getFolder(), file.getType());
        continue;
      }

      if (!param.getType().getName().equals(file.getType())) {
        if (!FileUtils.isFileExtensionAllwed(file.getName(),
                configuration.getTypes().get(file.getType()))) {
          param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION,
                  file.getName(), file.getFolder(), file.getType());
          continue;
        }
      }

      if (FileUtils.isFileHidden(file.getName(), configuration)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }

      if (FileUtils.isDirectoryHidden(file.getFolder(), configuration)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST;
      }

      if (!configuration.getAccessControl().hasPermission(file.getType(), file.getFolder(),
              param.getUserRole(),
              AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW)) {
        return Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED;
      }
      Path sourceFile = Paths.get(configuration.getTypes().get(file.getType()).getPath(),
              file.getFolder(), file.getName());
      Path destFile = Paths.get(param.getType().getPath(),
              param.getCurrentFolder(), file.getName());

      Path sourceThumb = Paths.get(configuration.getThumbsPath(), file.getType(),
              file.getFolder(), file.getName());
      try {
        if (!Files.isRegularFile(sourceFile)) {
          param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_FILE_NOT_FOUND,
                  file.getName(), file.getFolder(), file.getType());
          continue;
        }
        if (!param.getType().getName().equals(file.getType())) {
          long maxSize = param.getType().getMaxSize();
          if (maxSize != 0 && maxSize < Files.size(sourceFile)) {
            param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG,
                    file.getName(), file.getFolder(), file.getType());
            continue;
          }
        }
        if (sourceFile.equals(destFile)) {
          param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_SOURCE_AND_TARGET_PATH_EQUAL,
                  file.getName(), file.getFolder(), file.getType());
        } else if (Files.exists(destFile)) {
          if (file.getOptions() != null
                  && file.getOptions().contains("overwrite")) {
            if (!handleOverwrite(sourceFile, destFile)) {
              param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                      file.getName(), file.getFolder(),
                      file.getType());
            } else {
              param.filesMovedPlus();
              FileUtils.delete(sourceThumb);
            }
          } else if (file.getOptions() != null
                  && file.getOptions().contains("autorename")) {
            if (!handleAutoRename(sourceFile, destFile)) {
              param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                      file.getName(), file.getFolder(),
                      file.getType());
            } else {
              param.filesMovedPlus();
              FileUtils.delete(sourceThumb);
            }
          } else {
            param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ALREADY_EXIST,
                    file.getName(), file.getFolder(), file.getType());
          }
        } else if (FileUtils.copyFromSourceToDestFile(sourceFile, destFile,
                true)) {
          param.filesMovedPlus();
          moveThumb(file, param, configuration);
        }
      } catch (SecurityException | IOException e) {
        log.error("", e);
        param.appendErrorNodeChild(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED,
                file.getName(), file.getFolder(), file.getType());
      }

    }
    param.setAddMoveNode(true);
    if (param.hasErrors()) {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_MOVE_FAILED;
    } else {
      return Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE;
    }
  }

  /**
   * Handles autorename option.
   *
   * @param sourceFile source file to move from.
   * @param destFile destination file to move to.
   * @return true if moved correctly
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
        log.debug("prepare move file '{}' to '{}'", sourceFile, newDestFile);
        return (FileUtils.copyFromSourceToDestFile(sourceFile,
                newDestFile, true));
      }
    }
  }

  /**
   * Handles overwrite option.
   *
   * @param sourceFile source file to move from.
   * @param destFile destination file to move to.
   * @return true if moved correctly
   * @throws IOException when ioerror occurs
   */
  private boolean handleOverwrite(Path sourceFile, Path destFile)
          throws IOException {
    return FileUtils.delete(destFile)
            && FileUtils.copyFromSourceToDestFile(sourceFile, destFile,
                    true);
  }

  /**
   * move thumb file.
   *
   * @param file file to move.
   * @throws IOException when ioerror occurs
   */
  private void moveThumb(FilePostParam file, MoveFilesParameter param, IConfiguration configuration) throws IOException {
    Path sourceThumbFile = Paths.get(configuration.getThumbsPath(),
            file.getType(), file.getFolder(), file.getName());
    Path destThumbFile = Paths.get(configuration.getThumbsPath(),
            param.getType().getName(), param.getCurrentFolder(), file.getName()
    );

    FileUtils.copyFromSourceToDestFile(sourceThumbFile, destThumbFile,
            true);
  }

  @Override
  protected void initParams(MoveFilesParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setMovedAll(request.getParameter("moved") != null ? Integer.parseInt(request.getParameter("moved")) : 0);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles());
  }

}
