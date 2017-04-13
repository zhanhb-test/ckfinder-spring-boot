package com.github.zhanhb.ckfinder.connector.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 * @author zhanhb
 */
class DeleteVisitor extends SimpleFileVisitor<Path> {

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
    try {
      Files.deleteIfExists(dir);
      return FileVisitResult.SKIP_SUBTREE;
    } catch (IOException ex) {
      return FileVisitResult.CONTINUE;
    }
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    Files.deleteIfExists(file);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    if (exc != null) {
      throw exc;
    }
    Files.deleteIfExists(dir);
    return FileVisitResult.CONTINUE;
  }

}
