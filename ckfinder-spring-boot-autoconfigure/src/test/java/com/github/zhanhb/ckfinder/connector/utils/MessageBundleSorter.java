package com.github.zhanhb.ckfinder.connector.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class MessageBundleSorter {

  public static void main(String[] args) throws IOException {
    URI uri = URI.create(MessageBundleSorter.class.getResource("LocalStrings.properties").toExternalForm());
    Path parent = Paths.get(uri).getParent();
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent)) {
      stream.forEach(p -> {
        if (p.getFileName().toString().endsWith(".properties")) {
          System.out.println(p);
          try {
            Properties properties = new PropertiesImpl();
            try (InputStream is = Files.newInputStream(p)) {
              properties.load(is);
            }
            try (OutputStream is = Files.newOutputStream(p)) {
              properties.store(is, null);
            }
          } catch (IOException ex) {
            throw new UncheckedIOException(ex);
          }
        }
      });
    }
  }

  private static class PropertiesImpl extends Properties {

    private static final long serialVersionUID = 0L;

    @Override
    public Enumeration<Object> keys() {
      @SuppressWarnings("UseOfObsoleteCollectionType")
      Vector<Object> v = new Vector<>(Collections.list(super.keys()));
      v.sort(Comparator.comparing(Object::toString, Comparator.comparingInt(Integer::parseInt)));
      return v.elements();
    }

    @Override
    public Object clone() {
      return super.clone();
    }

  }
}
