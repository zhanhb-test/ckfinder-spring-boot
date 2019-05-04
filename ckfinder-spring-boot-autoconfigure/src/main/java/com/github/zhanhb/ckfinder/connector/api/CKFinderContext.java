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
package com.github.zhanhb.ckfinder.connector.api;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Interface of context
 */
public interface CKFinderContext extends CommandFactory, EventHandler, LicenseFactory {

  /**
   * gets user role name sets in config.
   *
   * @return role name
   */
  String getUserRoleName();

  /**
   * null if not exists
   *
   * @param typeName the type name of the resource
   * @return resource configuration
   */
  ResourceType getResource(String typeName);

  /**
   * @return all the resources
   */
  Collection<ResourceType> getResources();

  /**
   * gets image properties
   *
   * @return image properties
   */
  ImageProperties getImage();

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
  ThumbnailProperties getThumbnail();

  /**
   * check if dirname matches hidden folder regex.
   *
   * @param dirName dir name
   * @return true if matches.
   */
  boolean isDirectoryHidden(String dirName);

  /**
   * check if filename matches hidden file regex.
   *
   * @param fileName file name
   * @return true if matches.
   */
  boolean isFileHidden(String fileName);

  /**
   * if check double extensions.
   *
   * @return check double extensions
   */
  boolean isDoubleFileExtensionsAllowed();

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
   * gets param SecureImageUploads.
   *
   * @return true if is set
   */
  boolean isSecureImageUploads();

  /**
   * gets a list of default resource types.
   *
   * @return list of default resource types
   */
  Set<String> getDefaultResourceTypes();

  /**
   *
   * @return the access control
   */
  AccessControl getAccessControl();

}
