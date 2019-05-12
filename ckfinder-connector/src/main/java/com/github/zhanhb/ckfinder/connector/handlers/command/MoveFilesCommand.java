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
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.CopyMoveParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.MoveFilesElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;
import com.github.zhanhb.ckfinder.connector.support.FileItem;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>MoveFiles</code> command.
 */
@Slf4j
public class MoveFilesCommand extends FailAtEndXmlCommand<CopyMoveParameter> implements IPostCommand {

  @Override
  protected void createXml(CopyMoveParameter param, CommandContext cmdContext, ConnectorElement.Builder rootElement)
          throws ConnectorException {
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME
            | AccessControl.FILE_DELETE
            | AccessControl.FILE_UPLOAD);
    cmdContext.checkFilePostParam(param.getFiles(), AccessControl.FILE_VIEW | AccessControl.FILE_DELETE);

    ErrorListResult.Builder builder = ErrorListResult.builder();
    ResourceType cmdContextType = cmdContext.getType();

    int success = 0;

    for (FileItem file : param.getFiles()) {
      String name = file.getName();
      ResourceType type = file.getType();
      if (!FileUtils.isFileExtensionAllowed(name, cmdContextType)) {
        builder.appendError(file, ErrorCode.INVALID_EXTENSION);
        continue;
      }
      // check #4 (extension) - when move to another resource type,
      //double check extension
      if (cmdContextType != type
              && !FileUtils.isFileExtensionAllowed(name, type)) {
        builder.appendError(file, ErrorCode.INVALID_EXTENSION);
        continue;
      }

      Path sourceFile = file.toPath();
      Path destFile = cmdContext.resolve(name);

      BasicFileAttributes attrs;
      try {
        attrs = Files.readAttributes(sourceFile, BasicFileAttributes.class);
      } catch (IOException ex) {
        builder.appendError(file, ErrorCode.FILE_NOT_FOUND);
        continue;
      }
      if (!attrs.isRegularFile()) {
        builder.appendError(file, ErrorCode.FILE_NOT_FOUND);
      }
      if (cmdContextType != type) {
        if (cmdContextType.isFileSizeOutOfRange(attrs.size())) {
          builder.appendError(file, ErrorCode.UPLOADED_TOO_BIG);
          continue;
        }
        // fail through
      }
      try {
        if (Files.isSameFile(sourceFile, destFile)) {
          builder.appendError(file, ErrorCode.SOURCE_AND_TARGET_PATH_EQUAL);
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
            builder.appendError(file, ErrorCode.ACCESS_DENIED);
            continue;
          }
        } else if (options != null && options.contains("autorename")) {
          destFile = handleAutoRename(sourceFile, destFile);
          if (destFile == null) {
            builder.appendError(file, ErrorCode.ACCESS_DENIED);
            continue;
          }
        } else {
          builder.appendError(file, ErrorCode.ALREADY_EXIST);
          continue;
        }
      } catch (IOException e) {
        log.error("", e);
        builder.appendError(file, ErrorCode.ACCESS_DENIED);
        continue;
      }
      ++success;
      moveThumb(file, sourceFile.relativize(destFile));
    }
    builder.ifError(ErrorCode.MOVE_FAILED).addErrorsTo(rootElement);
    rootElement.result(MoveFilesElement.builder()
            .moved(success)
            .movedTotal(param.getAll() + success)
            .build());
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
        log.debug("move file, '{}'->'{}'", sourceFile, newDestFile);
        Files.move(sourceFile, newDestFile);
        // can't be in one if, because when error in
        // move file occurs then it will be an infinity loop
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
  private void moveThumb(FileItem file, Path relation) {
    file.toThumbnailPath().ifPresent(sourceThumbFile -> {
      Path destThumbFile = sourceThumbFile.resolve(relation).normalize();
      log.debug("move thumb, '{}'->'{}'", sourceThumbFile, destThumbFile);
      try {
        Path dir = destThumbFile.getParent();
        if (dir != null) {
          Files.createDirectories(dir);
        }
        Files.move(sourceThumbFile, destThumbFile, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException ex) {
        log.info("move thumbnail failed", ex);
      }
    });
  }

  @Override
  protected CopyMoveParameter parseParameters(HttpServletRequest request, CKFinderContext context) {
    String moved = request.getParameter("moved");
    int all = moved != null ? Integer.parseInt(moved) : 0;
    List<FileItem> files = RequestFileHelper.getFilesList(request, context);
    return CopyMoveParameter.builder().files(files).all(all).build();
  }

}
