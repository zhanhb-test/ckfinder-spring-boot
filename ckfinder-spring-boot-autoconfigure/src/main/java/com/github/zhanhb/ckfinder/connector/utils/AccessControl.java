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
package com.github.zhanhb.ckfinder.connector.utils;

import com.github.zhanhb.ckfinder.connector.data.AccessControlLevel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Class to generate ACL values.
 */
public class AccessControl {

  /**
   * Folder view mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FOLDER_VIEW = 1;
  /**
   * Folder create mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FOLDER_CREATE = 1 << 1;
  /**
   * Folder rename mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FOLDER_RENAME = 1 << 2;
  /**
   * Folder delete mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FOLDER_DELETE = 1 << 3;
  /**
   * File view mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FILE_VIEW = 1 << 4;
  /**
   * File upload mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FILE_UPLOAD = 1 << 5;
  /**
   * File rename mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FILE_RENAME = 1 << 6;
  /**
   * File delete mask.
   */
  public static final int CKFINDER_CONNECTOR_ACL_FILE_DELETE = 1 << 7;

  private final Map<CheckEntry, AclContext> aclMap = new ConcurrentHashMap<>(4);

  /**
   * check ACL for folder.
   *
   * @param resourceType resource type name
   * @param folder folder name
   * @param acl mask to check.
   * @param role user role
   * @return true if acl flag is true
   */
  public boolean hasPermission(String resourceType, String folder, String role, int acl) {
    return (getAcl(resourceType, folder, role) & acl) == acl;
  }

  public void addPermission(AccessControlLevel acl) {
    aclContext(acl.getResourceType(), acl.getRole()).getMask(acl.getFolder()).setValue(acl.getMask());
  }

  private AclContext getAclContext(String type, String role) {
    return aclMap.get(CheckEntry.builder().role(role).type(type).build());
  }

  private AclContext aclContext(String type, String role) {
    return aclMap.computeIfAbsent(CheckEntry.builder().role(role).type(type).build(), __ -> new AclContext());
  }

  /**
   * Checks ACL for given role.
   *
   * @param resourceType resource type
   * @param folder current folder
   * @param role current user role
   * @return mask value
   */
  public int getAcl(String resourceType, String folder, String role) {
    int acl = getAcl0("*", "*", folder) | getAcl0("*", resourceType, folder);
    if (role != null) {
      acl |= getAcl0(role, "*", folder) | getAcl0(role, resourceType, folder);
    }
    return acl;
  }

  private int getAcl0(String role, String resourceType, String path) {
    AclContext aclContext = getAclContext(resourceType, role);
    return aclContext == null ? 0 : aclContext.closest(path).getEffectiveValue();
  }

  /**
   * simple check ACL entry.
   */
  @Builder(builderClassName = "Builder")
  @EqualsAndHashCode
  @RequiredArgsConstructor
  private static class CheckEntry {

    final String role;
    final String type;

  }

}
