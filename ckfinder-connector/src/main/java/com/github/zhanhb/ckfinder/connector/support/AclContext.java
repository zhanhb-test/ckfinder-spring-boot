package com.github.zhanhb.ckfinder.connector.support;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author zhanhb
 */
public class AclContext {

  private final Map<String, Mask> aclCache;
  private final Mask root;

  AclContext() {
    aclCache = new ConcurrentHashMap<>(10);
    root = new Mask("/", null, 0);
    aclCache.put(root.getPath(), root);
  }

  int size() {
    return aclCache.size();
  }

  public Mask getRoot() {
    return root;
  }

  Mask getMask(String path) {
    Objects.requireNonNull(path);
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("name must starts with slash");
    }

    // check if the desired mask exists, if it does, return it
    // without further ado.
    Mask childMask = aclCache.get(path);
    // if we have the child, then let us return it without wasting time
    if (childMask != null) {
      return childMask;
    }

    int i = 0;
    Mask mask = root;

    // if the desired mask does not exist, them create all the masks
    // in between as well (if they don't already exist)
    String childName;
    while (true) {
      int h = path.indexOf('/', i);
      if (h == -1) {
        childName = path;
      } else {
        childName = path.substring(0, h);
      }
      // move i left of the last point
      i = h + 1;
      //noinspection SynchronizationOnLocalVariableOrMethodParameter
      synchronized (mask) {
        childMask = mask.getChildByName(childName);
        if (childMask == null) {
          childMask = mask.createChildByName(childName);
          aclCache.put(childName, childMask);
        }
      }
      mask = childMask;
      if (h == -1) {
        return childMask;
      }
    }
  }

  Mask closest(String path) {
    Objects.requireNonNull(path);
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("name must starts with slash");
    }

    for (String parentPath = path;;) {
      Mask parentMask = aclCache.get(parentPath);
      // if we have the child, then let us return it without wasting time
      if (parentMask != null) {
        return parentMask;
      }
      int h = parentPath.lastIndexOf('/');
      if (h <= 0) {
        return root;
      }
      parentPath = parentPath.substring(0, h);
    }
  }

}
