package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.configuration.ConnectorError;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DetailError;
import com.github.zhanhb.ckfinder.connector.handlers.response.Errors;

/**
 *
 * @author zhanhb
 */
public class ErrorListXmlParameter extends Parameter {

  /**
   *
   * errors list.
   */
  private final Errors.Builder errorsBuilder = Errors.builder();
  private boolean hasError;

  /**
   * save errors node to list.
   *
   * @param errorCode error code
   * @param name file name
   * @param folder current folder
   * @param type resource type
   */
  public void appendErrorNodeChild(ConnectorError errorCode, String name, String folder, String type) {
    errorsBuilder.error(DetailError.builder().type(type).name(name).folder(folder).code(errorCode.getCode()).build());
    hasError = true;
  }

  /**
   * checks if error list contains errors.
   *
   * @return true if there are any errors.
   */
  public boolean hasError() {
    return hasError;
  }

  /**
   * add all error nodes from saved list to xml.
   *
   * @param rootElement XML root element
   */
  public void addErrorsTo(Connector.Builder rootElement) {
    if (hasError) {
      rootElement.errors(errorsBuilder.build());
    }
  }

}
