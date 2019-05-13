package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class CopyMoveHelper {

  static int process(CommandContext cmdContext, List<FileItem> files, ErrorListResult.Builder builder, CopyOrMove fun, String action) {
    ResourceType cmdContextType = cmdContext.getType();
    int success = 0;
    for (FileItem file : files) {
      String name = file.getName();
      ResourceType type = file.getType();
      if (!FileUtils.isFileExtensionAllowed(name, cmdContextType)) {
        builder.appendError(file, ErrorCode.INVALID_EXTENSION);
        continue;
      }
      // check #4 (extension) - when copy/move to another resource type,
      //double check extension
      if (!FileUtils.isFileExtensionAllowed(name, type)) {
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
        continue;
      }
      if (cmdContextType.isFileSizeOutOfRange(attrs.size())) {
        builder.appendError(file, ErrorCode.UPLOADED_TOO_BIG);
        continue;
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
        fun.apply(sourceFile, destFile);
      } catch (FileAlreadyExistsException e) {
        String options = file.getOptions();
        if (options != null && options.contains("overwrite")) {
          try {
            fun.apply(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
          } catch (IOException ex) {
            log.error("{} file failed", action, ex);
            builder.appendError(file, ErrorCode.ACCESS_DENIED);
            continue;
          }
        } else if (options != null && options.contains("autorename")) {
          destFile = handleAutoRename(sourceFile, destFile, fun, action);
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
      Path relation = sourceFile.relativize(destFile);
      file.toThumbnailPath().ifPresent(sourceThumbFile -> {
        Path destThumbFile = sourceThumbFile.resolve(relation).normalize();
        log.debug("{} thumb from '{}' to '{}'", action, sourceThumbFile, destThumbFile);
        try {
          Path dir = destThumbFile.getParent();
          if (dir != null) {
            Files.createDirectories(dir);
          }
          fun.apply(sourceThumbFile, destThumbFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
          log.info("{} thumbnail failed", action, ex);
        }
      });
    }
    return success;
  }

  /**
   * Handles auto rename option.
   *
   * @param sourceFile source file to copy/move from.
   * @param destFile destination file to copy/move to.
   * @return the new destination path
   */
  private static Path handleAutoRename(Path sourceFile, Path destFile, CopyOrMove fun, String action) {
    String fileName = destFile.getFileName().toString();
    String[] nameAndExtension = FileUtils.getLongNameAndExtension(fileName);
    String name = nameAndExtension[0];
    StringBuilder sb = new StringBuilder(name).append("(");
    String suffix = ")." + nameAndExtension[1];
    int len = sb.length();
    for (int counter = 1;; counter++) {
      sb.append(counter).append(suffix);
      String newFileName = sb.toString();
      Path newDestFile = destFile.resolveSibling(newFileName);
      try {
        log.debug("prepare {} file '{}' to '{}'", action, sourceFile, newDestFile);
        fun.apply(sourceFile, newDestFile);
        // can'apply be in one if, because when error in
        // copy/move file occurs then it will be infinity loop
        return newDestFile;
      } catch (FileAlreadyExistsException ignored) {
      } catch (IOException ex) {
        return null;
      }
      sb.setLength(len);
    }
  }

}
