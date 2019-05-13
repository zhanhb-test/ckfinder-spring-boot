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
package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.CommandFactory;
import com.github.zhanhb.ckfinder.connector.api.EventHandler;
import com.github.zhanhb.ckfinder.connector.api.ImageProperties;
import com.github.zhanhb.ckfinder.connector.api.LicenseFactory;
import com.github.zhanhb.ckfinder.connector.api.PluginInfo;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.api.ThumbnailProperties;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.Delegate;
import lombok.experimental.NonFinal;

@Builder(builderClassName = "Builder")
@SuppressWarnings({"FinalClass", "PublicInnerClass"})
@Value
public class DefaultCKFinderContext implements CKFinderContext {

  private boolean enabled;
  @Delegate
  @NonNull
  private LicenseFactory licenseFactory;
  @NonNull
  private ImageProperties image;
  @Singular
  private Map<String, ResourceType> types;
  @Nullable
  private ThumbnailProperties thumbnail;
  @Singular
  private List<String> hiddenFolders;
  @Singular
  private List<String> hiddenFiles;
  private boolean doubleFileExtensionsDisallowed;
  private boolean forceAscii;
  private boolean checkSizeAfterScaling;
  @NonNull
  private String userRoleName;
  @Nullable
  private String publicPluginNames;
  private boolean secureImageUploads;
  @Singular
  private Set<String> defaultResourceTypes;
  private boolean disallowUnsafeCharacters;
  private boolean csrfProtectionEnabled;
  @Delegate
  @NonNull
  private EventHandler events;
  @Singular
  private List<PluginInfo> pluginInfos;
  @Delegate
  @NonNull
  private AccessControl accessControl;
  @Delegate
  @NonNull
  private CommandFactory commandFactory;
  @NonFinal
  private transient Pattern fileHiddenPattern;
  @NonFinal
  private transient Pattern directoryHiddenPattern;

  @Override
  public ResourceType getResource(String typeName) {
    return types.get(typeName);
  }

  @Override
  public Collection<ResourceType> getResources() {
    return types.values();
  }

  @Override
  public boolean isDirectoryHidden(String dirName) {
    if (dirName == null || dirName.isEmpty()) {
      return false;
    }
    String dir = PathUtils.normalize(dirName);
    StringTokenizer sc = new StringTokenizer(dir, "/");
    Pattern pattern = directoryHiddenPattern;
    if (pattern == null) {
      pattern = Pattern.compile(buildHiddenFileOrFolderRegex(hiddenFolders));
      directoryHiddenPattern = pattern;
    }
    while (sc.hasMoreTokens()) {
      if (pattern.matcher(sc.nextToken()).matches()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isFileHidden(String fileName) {
    Pattern pattern = fileHiddenPattern;
    if (pattern == null) {
      pattern = Pattern.compile(buildHiddenFileOrFolderRegex(hiddenFiles));
      fileHiddenPattern = pattern;
    }
    return pattern.matcher(fileName).matches();
  }

  /**
   * get hidden folder regex pattern.
   *
   * @param hiddenList list of hidden file or files patterns.
   * @return full folder regex pattern
   */
  private String buildHiddenFileOrFolderRegex(List<String> hiddenList) {
    return hiddenList.stream()
            .map(item -> item.replace(".", "\\.").replace("*", ".+").replace('?', '.'))
            .collect(Collectors.joining("|", "(", ")"));
  }

  public static class Builder {

    Builder() {
      csrfProtectionEnabled = true;
      checkSizeAfterScaling = true;
      userRoleName = "";
    }

  }

}
