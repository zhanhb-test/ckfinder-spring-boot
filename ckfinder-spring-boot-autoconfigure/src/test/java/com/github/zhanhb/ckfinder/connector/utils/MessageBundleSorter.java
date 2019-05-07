package com.github.zhanhb.ckfinder.connector.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;

public class MessageBundleSorter {

  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static void main(String[] args) throws IOException {
    String pkg = MessageBundleSorter.class.getPackage().getName().replace('.', '/');
    Path parent = Paths.get("src/main/resources", pkg);
    DirectoryStream.Filter<Path> filter = pth
            -> pth.getFileName().toString().matches("LocalStrings.*\\.properties");
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent, filter)) {
      stream.forEach(p -> {
        System.out.println(p);
        try {
          Properties properties = new KeySortedProperties();
          try (InputStream is = Files.newInputStream(p)) {
            properties.load(is);
          }
          try (OutputStream is = Files.newOutputStream(p)) {
            properties.store(is, null);
          }
        } catch (IOException ex) {
          throw new UncheckedIOException(ex);
        }
      });
    }
  }

  @SuppressWarnings("CloneableImplementsClone")
  private static class KeySortedProperties extends Properties {

    private static final long serialVersionUID = 0L;

    @Override
    public Enumeration<Object> keys() {
      @SuppressWarnings("UseOfObsoleteCollectionType")
      java.util.Vector<Object> v = new java.util.Vector<>(Collections.list(super.keys()));
      v.sort(Comparator.comparing(Object::toString, Comparator.comparingInt(Integer::parseInt)));
      return v.elements();
    }

  }

}
