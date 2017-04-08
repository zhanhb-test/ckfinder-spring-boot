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
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Interface for configuration.
 */
public interface IConfiguration {

  /**
   * gets user role name sets in config.
   *
   * @return role name
   */
  String getUserRoleName();

  /**
   * gets resources map types with names as map keys.
   *
   * @return resources map
   */
  Map<String, ResourceType> getTypes();

  /**
   * returns license.
   *
   * @param host the http host
   * @return license
   */
  License getLicense(String host);

  /**
   * gets image max width.
   *
   * @return max image height
   */
  int getImgWidth();

  /**
   * get image max height.
   *
   * @return max image height
   */
  int getImgHeight();

  /**
   * get image quality.
   *
   * @return image quality
   */
  float getImgQuality();

  /**
   * check if connector is enabled.
   *
   * @return if connector is enabled
   */
  boolean isEnabled();

  /**
   * get the thumbnail properties, null if thumbnail is disabled.
   *
   * @return the thumbnail properties
   */
  @Nullable
  Thumbnail getThumbnail();

  /**
   * check if dirname matches configuration hidden folder regex.
   *
   * @param dirName dir name
   * @return true if matches.
   */
  boolean isDirectoryHidden(String dirName);

  /**
   * check if filename matches configuration hidden file regex.
   *
   * @param fileName file name
   * @return true if matches.
   */
  boolean isFileHidden(String fileName);

  /**
   * get double extensions configuration.
   *
   * @return configuration value.
   */
  boolean isCheckDoubleFileExtensions();

  /**
   * flag to check if force ASCII.
   *
   * @return true if force ASCII.
   */
  boolean isForceAscii();

  /**
   * Checks if disallowed characters in file and folder names are turned on.
   *
   * @return disallowUnsafeCharacters
   */
  boolean isDisallowUnsafeCharacters();

  /**
   * flag if check image size after resizing image.
   *
   * @return true if check.
   */
  boolean isCheckSizeAfterScaling();

  /**
   * gets a list of plugins.
   *
   * @return list of plugins.
   */
  String getPublicPluginNames();

  /**
   * gets events.
   *
   * @return events.
   */
  Events getEvents();

  /**
   * gets param SecureImageUploads.
   *
   * @return true if is set
   */
  boolean isSecureImageUploads();

  /**
   * gets html extensions.
   *
   * @return list of html extensions.
   */
  List<String> getHtmlExtensions();

  /**
   * gets a list of default resource types.
   *
   * @return list of default resource types
   */
  Set<String> getDefaultResourceTypes();

  /**
   *
   * @return the configuration
   */
  AccessControl getAccessControl();

  /**
   *
   * @return the command factory
   */
  CommandFactory getCommandFactory();

}
