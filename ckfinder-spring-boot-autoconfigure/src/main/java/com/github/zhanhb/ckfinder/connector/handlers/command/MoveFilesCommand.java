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

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CopyMoveParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.MoveFiles;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
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
 * Class to handle <code>MoveFiles</code> command.
 */
@Slf4j
public class MoveFilesCommand extends ErrorListXmlCommand<CopyMoveParameter> implements IPostCommand {

  @Override
  protected void addResultNode(Connector.Builder rootElement, CopyMoveParameter param) {
    rootElement.result(MoveFiles.builder()
            .moved(param.getNfiles())
            .movedTotal(param.getAll() + param.getNfiles())
            .build());
  }

  @Override
  protected ErrorCode getDataForXml(CopyMoveParameter param, CommandContext cmdContext)
          throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME
            | AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD);

    for (FilePostParam file : param.getFiles()) {
      if (!FileUtils.isFileNameValid(file.getName())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }
      if (Pattern.compile(Constants.INVALID_PATH_REGEX).matcher(
              file.getFolder()).find()) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }
      if (file.getType() == null) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }
      if (file.getFolder() == null || file.getFolder().isEmpty()) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (context.isDirectoryHidden(file.getFolder())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      if (context.isFileHidden(file.getName())) {
        cmdContext.throwException(ErrorCode.INVALID_REQUEST);
      }

      cmdContext.checkAllPermission(file.getType(), file.getFolder(),
              AccessControl.FILE_VIEW | AccessControl.FILE_DELETE);
    }

    for (FilePostParam file : param.getFiles()) {
      if (!FileUtils.isFileExtensionAllowed(file.getName(), cmdContext.getType())) {
        param.appendError(file, ErrorCode.INVALID_EXTENSION);
        continue;
      }
      // check #4 (extension) - when move to another resource type,
      //double check extension
      if (cmdContext.getType() != file.getType()
              && !FileUtils.isFileExtensionAllowed(file.getName(), file.getType())) {
        param.appendError(file, ErrorCode.INVALID_EXTENSION);
        continue;
      }

      Path sourceFile = getPath(file.getType().getPath(),
              file.getFolder(), file.getName());
      Path destFile = getPath(cmdContext.getType().getPath(),
              cmdContext.getCurrentFolder(), file.getName());

      BasicFileAttributes attrs;
      try {
        attrs = Files.readAttributes(sourceFile, BasicFileAttributes.class);
      } catch (IOException ex) {
        param.appendError(file, ErrorCode.FILE_NOT_FOUND);
        continue;
      }
      if (!attrs.isRegularFile()) {
        param.appendError(file, ErrorCode.FILE_NOT_FOUND);
      }
      if (cmdContext.getType() != file.getType()) {
        long maxSize = cmdContext.getType().getMaxSize();
        if (maxSize != 0 && maxSize < attrs.size()) {
          param.appendError(file, ErrorCode.UPLOADED_TOO_BIG);
          continue;
        }
        // fail through
      }
      try {
        if (Files.isSameFile(sourceFile, destFile)) {
          param.appendError(file, ErrorCode.SOURCE_AND_TARGET_PATH_EQUAL);
          continue;
        }
      } catch (IOException ex) {
        // usually no such file exception, ok
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
            param.appendError(file, ErrorCode.ACCESS_DENIED);
            continue;
          }
        } else if (options != null && options.contains("autorename")) {
          destFile = handleAutoRename(sourceFile, destFile);
          if (destFile == null) {
            param.appendError(file, ErrorCode.ACCESS_DENIED);
            continue;
          }
        } else {
          param.appendError(file, ErrorCode.ALREADY_EXIST);
          continue;
        }
      } catch (IOException e) {
        log.error("", e);
        param.appendError(file, ErrorCode.ACCESS_DENIED);
        continue;
      }
      param.increase();
      moveThumb(file, sourceFile.relativize(destFile));
    }

    param.setAddResultNode(true);
    if (param.hasError()) {
      return ErrorCode.MOVE_FAILED;
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
    String fileNameWithoutExtension = FileUtils.getNameWithoutLongExtension(fileName);
    String fileExtension = FileUtils.getLongExtension(fileName);
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
      } catch (FileAlreadyExistsException ignored) {
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
   */
  private void moveThumb(FilePostParam file, Path relation) {
    Path thumbnailPath = file.getType().getThumbnailPath();
    if (thumbnailPath != null) {
      Path sourceThumbFile = getPath(thumbnailPath, file.getFolder(), file.getName());
      Path destThumbFile = sourceThumbFile.resolve(relation).normalize();

      log.debug("move thumb from '{}' to '{}'", sourceThumbFile, destThumbFile);
      try {
        Files.move(sourceThumbFile, destThumbFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ex) {
        log.error("{}", ex.getMessage());
      }
    }
  }

  @Override
  protected CopyMoveParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    CopyMoveParameter param = new CopyMoveParameter();
    String moved = request.getParameter("moved");
    param.setAll(moved != null ? Integer.parseInt(moved) : 0);
    RequestFileHelper.addFilesListFromRequest(request, param.getFiles(), context);
    return param;
  }

}
