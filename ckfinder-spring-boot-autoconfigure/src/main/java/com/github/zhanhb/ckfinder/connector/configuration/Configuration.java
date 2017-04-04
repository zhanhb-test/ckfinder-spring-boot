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
package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.experimental.NonFinal;

@Builder(builderClassName = "Builder")
@SuppressWarnings({
  "CollectionWithoutInitialCapacity",
  "ReturnOfCollectionOrArrayField",
  "FinalClass",
  "PublicInnerClass"
})
@Value
public class Configuration implements IConfiguration {

  private boolean enabled;
  private LicenseFactory licenseFactory;
  private int imgWidth;
  private int imgHeight;
  private float imgQuality;
  @Singular
  private Map<String, ResourceType> types;
  @Nullable
  private Thumbnail thumbnail;
  @Singular
  private List<String> hiddenFolders;
  @Singular
  private List<String> hiddenFiles;
  private boolean checkDoubleFileExtensions;
  private boolean forceAscii;
  private boolean checkSizeAfterScaling;
  private String userRoleName;
  @Nullable
  private String publicPluginNames;
  private boolean secureImageUploads;
  @Singular
  private List<String> htmlExtensions;
  @Singular
  private Set<String> defaultResourceTypes;
  private boolean disallowUnsafeCharacters;
  @NonNull
  private Events events;
  @NonNull
  private AccessControl accessControl;
  @NonNull
  private CommandFactory commandFactory;
  @NonFinal
  private Pattern fileHiddenPattern;
  @NonFinal
  private Pattern directoryHiddenPattern;

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
    StringBuilder sb = new StringBuilder("(");
    for (String item : hiddenList) {
      if (sb.length() > 3) {
        sb.append("|");
      }

      sb.append("(");
      sb.append(item.replace(".", "\\.").replace("*", ".+").replace("?", "."));
      sb.append(")");
    }
    sb.append(")+");
    return sb.toString();
  }

  @Override
  public License getLicense(HttpServletRequest request) {
    return licenseFactory.getLicense(request);
  }

  public static class Builder {

    Builder() {
      imgWidth = Constants.DEFAULT_IMG_WIDTH;
      imgHeight = Constants.DEFAULT_IMG_HEIGHT;
      imgQuality = Constants.DEFAULT_IMG_QUALITY;
      userRoleName = "";
    }

    public Builder eventsFromPlugins(Collection<? extends Plugin> plugins) {
      PluginRegister register = new PluginRegister();
      for (Plugin plugin : plugins) {
        plugin.regist(register);
      }
      events(register.buildEvents());
      commandFactory(register.buildCommandFactory());
      publicPluginNames(register.getNames());
      return this;
    }
  }

}
