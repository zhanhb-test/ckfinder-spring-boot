package com.github.zhanhb.ckfinder.connector.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author zhanhb
 */
public class Mask {

  private final Mask parent;
  private final String path;
  private int effectiveValue;
  private Integer value;
  private List<Mask> childrenList;

  Mask(String path, Mask parent, int effectiveValue) {
    this.path = path;
    this.parent = parent;
    this.effectiveValue = effectiveValue;
  }

  public String getPath() {
    return path;
  }

  Mask getChildByName(final String childPath) {
    if (childrenList == null) {
      return null;
    } else {
      for (final Mask childMask_i : this.childrenList) {
        final String childPath_i = childMask_i.getPath();

        if (childPath.equals(childPath_i)) {
          return childMask_i;
        }
      }
      // no child found
      return null;
    }
  }

  public synchronized void setValue(Integer newValue) {
    if (Objects.equals(value, newValue)) {
      // nothing to do;
      return;
    }
    value = newValue;
    if (newValue == null) {
      effectiveValue = parent == null ? 0 : parent.getEffectiveValue();
    } else {
      effectiveValue = newValue;
    }

    if (childrenList != null) {
      for (Mask child : childrenList) {
        child.handleParentValueChange(effectiveValue);
      }
    }
  }

  int getEffectiveValue() {
    return effectiveValue;
  }

  private synchronized void handleParentValueChange(int newParentValue) {
    if (value == null) {
      effectiveValue = newParentValue;

      // propagate the parent levelInt change to this logger's children
      if (childrenList != null) {
        for (Mask child : childrenList) {
          child.handleParentValueChange(newParentValue);
        }
      }
    }
  }

  Mask createChildByName(final String childName) {
    int i_index = childName.indexOf('/', path.length() + 1);
    if (i_index != -1) {
      throw new IllegalArgumentException();
    }

    if (childrenList == null) {
      childrenList = new ArrayList<>(1);
    }
    Mask childMask = new Mask(childName, this, effectiveValue);
    childrenList.add(childMask);
    return childMask;
  }

}
