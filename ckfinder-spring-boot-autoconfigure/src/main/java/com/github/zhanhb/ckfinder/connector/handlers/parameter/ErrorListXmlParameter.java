package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DetailError;
import com.github.zhanhb.ckfinder.connector.handlers.response.Errors;
import com.github.zhanhb.ckfinder.connector.support.FilePostParam;

/**
 *
 * @author zhanhb
 */
public class ErrorListXmlParameter {

  /**
   *
   * errors list.
   */
  private Errors.Builder errorsBuilder;

  private boolean addResultNode;

  /**
   * save errors node to list.
   *
   * @param filePostParam the file who leads the error
   * @param errorCode error code
   */
  public void appendError(FilePostParam filePostParam, ErrorCode errorCode) {
    int code = errorCode.getCode();
    String name = filePostParam.getName();
    String folder = filePostParam.getFolder();
    String type = filePostParam.getType().getName();
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
    if (hasError()) {
      rootElement.errors(errorsBuilder.build());
    }
  }

  public boolean isAddResultNode() {
    return addResultNode;
  }

  public void setAddResultNode(boolean addResultNode) {
    this.addResultNode = addResultNode;
  }

}
