package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.DetailError;
import com.github.zhanhb.ckfinder.connector.handlers.response.Errors;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author zhanhb
 */
public class ErrorListXMLParameter extends Parameter {

  /**
   *
   * errors list.
   */
  private final List<DetailError> errorList = new ArrayList<>(4);

  /**
   * save errors node to list.
   *
   * @param errorCode error code
   * @param name file name
   * @param path current folder
   * @param type resource type
   */
  public void appendErrorNodeChild(int errorCode, String name, String path, String type) {
    errorList.add(DetailError.builder().type(type).name(name).folder(path).code(errorCode).build());
  }

  /**
   * checks if error list contains errors.
   *
   * @return true if there are any errors.
   */
  public boolean hasErrors() {
    return errorList.isEmpty();
  }

  /**
   * add all error nodes from saved list to xml.
   *
   * @param rootElement XML root element
   */
  public void addErrorsTo(Connector.Builder rootElement) {
    if (!errorList.isEmpty()) {
      rootElement.errors(Errors.builder().errors(errorList).build());
    }
  }

}
