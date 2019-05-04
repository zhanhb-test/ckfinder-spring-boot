package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class CommandContext {

  private final CKFinderContext cfCtx;
  private final String userRole;
  private final ResourceType type;
  private final String currentFolder;

  public void setResourceType(Connector.Builder builder) {
    if (type != null) {
      builder.resourceType(type.getName());
    }
  }

  public void checkType() throws ConnectorException {
    if (type == null) {
      throw new ConnectorException(ErrorCode.INVALID_TYPE);
    }
  }

  public void throwException(ErrorCode code) throws ConnectorException {
    throw new ConnectorException(code, type, currentFolder);
  }

  public Collection<ResourceType> typeToCollection() {
    if (type != null) {
      return Collections.singleton(type);
    } else {
      if (cfCtx.getDefaultResourceTypes().size() > 0) {
        Set<String> defaultResourceTypes = cfCtx.getDefaultResourceTypes();
        ArrayList<ResourceType> list = new ArrayList<>(defaultResourceTypes.size());
        for (String key : defaultResourceTypes) {
          ResourceType resourceType = cfCtx.getTypes().get(key);
          if (resourceType != null) {
            list.add(resourceType);
          }
        }
        return list;
      } else {
        return cfCtx.getTypes().values();
      }
    }
  }

}
