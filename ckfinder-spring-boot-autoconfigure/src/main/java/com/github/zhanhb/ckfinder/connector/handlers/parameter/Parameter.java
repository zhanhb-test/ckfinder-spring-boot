package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
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
  private String currentFolder;
  private ResourceType type;

  public void throwException(ConnectorError code) throws ConnectorException {
    throw new ConnectorException(this, code);
  }

}
