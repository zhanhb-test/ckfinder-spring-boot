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

/**
 * Class to generate Acl values.
 */
public interface AccessControl {

  /**
   * Folder view mask.
   */
  int FOLDER_VIEW = 1;

  /**
   * Folder create mask.
   */
  int FOLDER_CREATE = 1 << 1;

  /**
   * Folder rename mask.
   */
  int FOLDER_RENAME = 1 << 2;

  /**
   * Folder delete mask.
   */
  int FOLDER_DELETE = 1 << 3;

  /**
   * File view mask.
   */
  int FILE_VIEW = 1 << 4;

  /**
   * File upload mask.
   */
  int FILE_UPLOAD = 1 << 5;

  /**
   * File rename mask.
   */
  int FILE_RENAME = 1 << 6;

  /**
   * File delete mask.
   */
  int FILE_DELETE = 1 << 7;

  /**
   * check ACL for folder.
   *
   * @param resourceType resource type name
   * @param folder folder name
   * @param acl mask to check.
   * @param role user role
   * @return true if acl flag is true
   */
  default boolean hasPermission(String resourceType, String folder, String role, int acl) {
    return (getAcl(resourceType, folder, role) & acl) == acl;
  }

  /**
   * Checks ACL for given role.
   *
   * @param resourceType resource type
   * @param folder current folder
   * @param role current user role
   * @return mask value
   */
  int getAcl(String resourceType, String folder, String role);

}
