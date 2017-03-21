package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
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
  private Errors.Builder errorsBuilder;

  /**
   * save errors node to list.
   *
   * @param errorCode error code
   * @param name file name
   * @param folder current folder
   * @param type resource type
   */
  public void appendErrorNodeChild(ConnectorError errorCode, String name, String folder, String type) {
    int code = errorCode.getCode();
    if (errorsBuilder == null) {
      errorsBuilder = Errors.builder();
    }
    errorsBuilder.error(DetailError.builder().type(type).name(name).folder(folder).code(code).build());
  }

  /**
   * checks if error list contains errors.
   *
   * @return true if there are any errors.
   */
  public boolean hasError() {
    return errorsBuilder != null;
  }

  /**
   * add all error nodes from saved list to xml.
   *
   * @param rootElement XML root element
   */
  public void addErrorsTo(Connector.Builder rootElement) {
    if (errorsBuilder != null) {
      rootElement.errors(errorsBuilder.build());
    }
  }

}