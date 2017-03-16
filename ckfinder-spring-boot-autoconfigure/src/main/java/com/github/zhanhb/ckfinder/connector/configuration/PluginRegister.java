package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.data.FileUploadListener;
import com.github.zhanhb.ckfinder.connector.data.PluginInfoRegister;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author zhanhb
 */
public class PluginRegister {

  private final List<FileUploadListener> fileUploadListeners = new ArrayList<>(4);
  private final List<PluginInfoRegister> initCommandEventHandlers = new ArrayList<>(4);
  private final Set<String> names = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

  public void addFileUploadListener(FileUploadListener fileUploadListener) {
    Objects.requireNonNull(fileUploadListener);
    fileUploadListeners.add(fileUploadListener);
  }

  public void addPluginInfoRegister(PluginInfoRegister pluginInfoRegister) {
    Objects.requireNonNull(pluginInfoRegister);
    initCommandEventHandlers.add(pluginInfoRegister);
  }

  public void addName(String name) {
    if (name.matches("\\s+")) {
      throw new IllegalArgumentException("name should not be empty");
    }
    names.add(name);
  }

  Events buildEvents() {
    return new Events(new ArrayList<>(fileUploadListeners), new ArrayList<>(initCommandEventHandlers));
  }

  String getNames() {
    return names.stream().collect(Collectors.joining(","));
  }

}
