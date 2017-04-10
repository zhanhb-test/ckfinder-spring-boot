package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 *
 * @author zhanhb
 */
public class InMemoryAccessController implements AccessControl, Serializable {

  private static final long serialVersionUID = 1L;

  private final Map<CheckEntry, AclContext> aclMap = new ConcurrentHashMap<>(4);

  public void addPermission(AccessControlLevel acl) {
    aclContext(acl.getResourceType(), acl.getRole()).getMask(acl.getFolder()).setValue(acl.getMask());
  }

  private AclContext getAclContext(String type, String role) {
    return aclMap.get(new CheckEntry(role, type));
  }

  private AclContext aclContext(String type, String role) {
    return aclMap.computeIfAbsent(new CheckEntry(role, type), __ -> new AclContext());
  }

  /**
   * Checks ACL for given role.
   *
   * @param resourceType resource type
   * @param folder current folder
   * @param role current user role
   * @return mask value
   */
  @Override
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
  @EqualsAndHashCode
  @RequiredArgsConstructor
  @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
  private static class CheckEntry {

    private final String role;
    private final String type;

  }

}
