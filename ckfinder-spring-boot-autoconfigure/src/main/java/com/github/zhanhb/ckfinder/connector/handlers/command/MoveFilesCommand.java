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
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.MoveFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.MoveFiles;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>MoveFiles</code> command.
 */
@Slf4j
public class MoveFilesCommand extends ErrorListXmlCommand<MoveFilesParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, MoveFilesParameter param, IConfiguration configuration) {
    rootElement.result(MoveFiles.builder()
            .moved(param.getFilesMoved())
            .movedTotal(param.getMovedAll() + param.getFilesMoved())
            .build());
  }

  @Override
  protected ConnectorError getDataForXml(MoveFilesParameter param, IConfiguration configuration)
          throws ConnectorException {
    if (param.getType() == null) {
      throw new ConnectorException(ConnectorError.INVALID_TYPE);
    }

    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(),
            param.getUserRole(),
            AccessControl.FILE_RENAME
            | AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }

    for (FilePostParam file : param.getFiles()) {
      if (!FileUtils.isFileNameValid(file.getName())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }
      if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
              file.getFolder()).find()) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }
      if (file.getType() == null) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }
      if (file.getFolder() == null || file.getFolder().isEmpty()) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (configuration.isDirectoryHidden(file.getFolder())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (configuration.isFileHidden(file.getName())) {
        param.throwException(ConnectorError.INVALID_REQUEST);
      }

      if (!configuration.getAccessControl().hasPermission(file.getType().getName(),
              file.getFolder(), param.getUserRole(), AccessControl.FILE_VIEW | AccessControl.FILE_DELETE)) {
        param.throwException(ConnectorError.UNAUTHORIZED);
      }
    }

    for (FilePostParam file : param.getFiles()) {
      if (!FileUtils.isFileExtensionAllowed(file.getName(), param.getType())) {
        param.appendErrorNodeChild(ConnectorError.INVALID_EXTENSION,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }
      // check #4 (extension) - when move to another resource type,
      //double check extension
      if (param.getType() != file.getType()
              && !FileUtils.isFileExtensionAllowed(file.getName(), file.getType())) {
        param.appendErrorNodeChild(ConnectorError.INVALID_EXTENSION,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }

      Path sourceFile = getPath(file.getType().getPath(),
              file.getFolder(), file.getName());
      Path destFile = getPath(param.getType().getPath(),
              param.getCurrentFolder(), file.getName());

      BasicFileAttributes attrs;
      try {
        attrs = Files.readAttributes(sourceFile, BasicFileAttributes.class);
        if (!attrs.isRegularFile()) {
          throw new IOException();
        }
      } catch (IOException ex) {
        param.appendErrorNodeChild(ConnectorError.FILE_NOT_FOUND,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }
      if (param.getType() != file.getType()) {
        long maxSize = param.getType().getMaxSize();
        if (maxSize != 0 && maxSize < attrs.size()) {
          param.appendErrorNodeChild(ConnectorError.UPLOADED_TOO_BIG,
                  file.getName(), file.getFolder(), file.getType().getName());
          continue;
        }
        // fail through
      }
      if (Objects.equals(sourceFile, destFile)) {
        param.appendErrorNodeChild(ConnectorError.SOURCE_AND_TARGET_PATH_EQUAL,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }
      try {
        Files.move(sourceFile, destFile);
      } catch (FileAlreadyExistsException e) {
        String options = file.getOptions();
        if (options != null && options.contains("overwrite")) {
          try {
            Files.move(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException ex) {
            log.error("move file fail", ex);
            param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                    file.getName(), file.getFolder(), file.getType().getName());
            continue;
          }
        } else if (options != null && options.contains("autorename")) {
          destFile = handleAutoRename(sourceFile, destFile);
          if (destFile == null) {
            param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                    file.getName(), file.getFolder(), file.getType().getName());
            continue;
          }
        } else {
          param.appendErrorNodeChild(ConnectorError.ALREADY_EXIST,
                  file.getName(), file.getFolder(), file.getType().getName());
          continue;
        }
      } catch (IOException e) {
        log.error("", e);
        param.appendErrorNodeChild(ConnectorError.ACCESS_DENIED,
                file.getName(), file.getFolder(), file.getType().getName());
        continue;
      }
      param.filesMovedPlus();
      moveThumb(file, sourceFile.relativize(destFile), configuration);
    }

    param.setAddResultNode(true);
    if (param.hasError()) {
      return ConnectorError.MOVE_FAILED;
    } else {
      return null;
    }
  }

  /**
   * Handles auto rename option.
   *
   * @param sourceFile source file to move from.
   * @param destFile destination file to move to.
   * @return the new destination path
   */
  private Path handleAutoRename(Path sourceFile, Path destFile) {
    String fileName = destFile.getFileName().toString();
    String fileNameWithoutExtension = FileUtils.getFileNameWithoutExtension(fileName, false);
    String fileExtension = FileUtils.getFileExtension(fileName, false);
    for (int counter = 1;; counter++) {
      String newFileName = fileNameWithoutExtension
              + "(" + counter + ")."
              + fileExtension;
      Path newDestFile = destFile.resolveSibling(newFileName);
      try {
        log.debug("prepare move file '{}' to '{}'", sourceFile, newDestFile);
        Files.move(sourceFile, newDestFile);
        // can't be in one if=, because when error in
        // move file occurs then it will be infinity loop
        return newDestFile;
      } catch (FileAlreadyExistsException ex) {
      } catch (IOException ex) {
        return null;
      }
    }
  }

  /**
   * move thumb file.
   *
   * @param file file to move.
   * @param relation
   * @param configuration
   */
  private void moveThumb(FilePostParam file, Path relation,
          IConfiguration configuration) {
    Path sourceThumbFile = getPath(configuration.getThumbsPath(),
            file.getType().getName(), file.getFolder(), file.getName());
    Path destThumbFile = sourceThumbFile.resolve(relation).normalize();

    log.debug("move thumb from '{}' to '{}'", sourceThumbFile, destThumbFile);
    try {
      Files.move(sourceThumbFile, destThumbFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ex) {
      log.error("{}", ex.getMessage());
    }
  }

  @Override
  protected MoveFilesParameter popupParams(HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    MoveFilesParameter param = doInitParam(new MoveFilesParameter(), request, configuration);
    param.setMovedAll(request.getParameter("moved") != null ? Integer.parseInt(request.getParameter("moved")) : 0);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), configuration);
    return param;
  }

}
