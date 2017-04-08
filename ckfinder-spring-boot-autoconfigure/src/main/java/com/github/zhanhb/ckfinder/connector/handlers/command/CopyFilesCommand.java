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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CopyFilesParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CopyFiles;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>CopyFiles</code> command.
 */
@Slf4j
public class CopyFilesCommand extends ErrorListXmlCommand<CopyFilesParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, CopyFilesParameter param, IConfiguration configuration) {
    rootElement.result(CopyFiles.builder()
            .copied(param.getFilesCopied())
            .copiedTotal(param.getCopiedAll() + param.getFilesCopied())
            .build());
  }

  @Override
  protected ConnectorError getDataForXml(CopyFilesParameter param, IConfiguration configuration)
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
              file.getFolder(), param.getUserRole(), AccessControl.FILE_VIEW)) {
        param.throwException(ConnectorError.UNAUTHORIZED);
      }
    }

    for (FilePostParam file : param.getFiles()) {
      if (!FileUtils.isFileExtensionAllowed(file.getName(), param.getType())) {
        param.appendError(file, ConnectorError.INVALID_EXTENSION);
        continue;
      }
      // check #4 (extension) - when copy to another resource type,
      //double check extension
      if (param.getType() != file.getType()
              && !FileUtils.isFileExtensionAllowed(file.getName(), file.getType())) {
        param.appendError(file, ConnectorError.INVALID_EXTENSION);
        continue;
      }

      Path sourceFile = getPath(file.getType().getPath(),
              file.getFolder(), file.getName());
      Path destFile = getPath(param.getType().getPath(),
              param.getCurrentFolder(), file.getName());

      BasicFileAttributes attrs;
      try {
        attrs = Files.readAttributes(sourceFile, BasicFileAttributes.class);
      } catch (IOException ex) {
        param.appendError(file, ConnectorError.FILE_NOT_FOUND);
        continue;
      }
      if (!attrs.isRegularFile()) {
        param.appendError(file, ConnectorError.FILE_NOT_FOUND);
      }
      if (param.getType() != file.getType()) {
        long maxSize = param.getType().getMaxSize();
        if (maxSize != 0 && maxSize < attrs.size()) {
          param.appendError(file, ConnectorError.UPLOADED_TOO_BIG);
          continue;
        }
        // fail through
      }
      try {
        if (Files.isSameFile(sourceFile, destFile)) {
          param.appendError(file, ConnectorError.SOURCE_AND_TARGET_PATH_EQUAL);
          continue;
        }
      } catch (IOException ex) {
        // usually no such file exception, ok
      }
      try {
        Files.copy(sourceFile, destFile);
      } catch (FileAlreadyExistsException e) {
        String options = file.getOptions();
        if (options != null && options.contains("overwrite")) {
          try {
            Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException ex) {
            log.error("copy file fail", ex);
            param.appendError(file, ConnectorError.ACCESS_DENIED);
            continue;
          }
        } else if (options != null && options.contains("autorename")) {
          destFile = handleAutoRename(sourceFile, destFile);
          if (destFile == null) {
            param.appendError(file, ConnectorError.ACCESS_DENIED);
            continue;
          }
        } else {
          param.appendError(file, ConnectorError.ALREADY_EXIST);
          continue;
        }
      } catch (IOException e) {
        log.error("", e);
        param.appendError(file, ConnectorError.ACCESS_DENIED);
        continue;
      }
      param.filesCopiedPlus();
      copyThumb(file, sourceFile.relativize(destFile));
    }
    param.setAddResultNode(true);
    if (param.hasError()) {
      return ConnectorError.COPY_FAILED;
    } else {
      return null;
    }
  }

  /**
   * Handles auto rename option.
   *
   * @param sourceFile source file to copy from.
   * @param destFile destination file to copy to.
   * @return the new destination path
   */
  private Path handleAutoRename(Path sourceFile, Path destFile) {
    String fileName = destFile.getFileName().toString();
    String fileNameWithoutExtension = FileUtils.getNameWithoutLongExtension(fileName);
    String fileExtension = FileUtils.getLongExtension(fileName);
    for (int counter = 1;; counter++) {
      String newFileName = fileNameWithoutExtension
              + "(" + counter + ")."
              + fileExtension;
      Path newDestFile = destFile.resolveSibling(newFileName);
      try {
        log.debug("prepare copy file '{}' to '{}'", sourceFile, newDestFile);
        Files.copy(sourceFile, newDestFile);
        // can't be in one if=, because when error in
        // copy file occurs then it will be infinity loop
        return newDestFile;
      } catch (FileAlreadyExistsException ex) {
      } catch (IOException ex) {
        return null;
      }
    }
  }

  /**
   * copy thumb file.
   *
   * @param file file to copy.
   * @param relation
   */
  private void copyThumb(FilePostParam file, Path relation) {
    Path thumbnailPath = file.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path sourceThumbFile = getPath(thumbnailPath, file.getFolder(), file.getName());
      Path destThumbFile = sourceThumbFile.resolve(relation).normalize();

      log.debug("copy thumb from '{}' to '{}'", sourceThumbFile, destThumbFile);
      try {
        Files.copy(sourceThumbFile, destThumbFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ex) {
        log.error("{}", ex.getMessage());
      }
    }
  }

  @Override
  protected CopyFilesParameter popupParams(HttpServletRequest request, IConfiguration configuration) throws ConnectorException {
    CopyFilesParameter param = doInitParam(new CopyFilesParameter(), request, configuration);
    param.setCopiedAll(request.getParameter("copied") != null ? Integer.parseInt(request.getParameter("copied")) : 0);

    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), configuration);
    return param;
  }

}
