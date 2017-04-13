package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class Parameter {

  private String userRole;
  private ResourceType type;
  private String currentFolder;

  public void throwException(ErrorCode code) throws ConnectorException {
    throw new ConnectorException(code, type, currentFolder);
  }

}
