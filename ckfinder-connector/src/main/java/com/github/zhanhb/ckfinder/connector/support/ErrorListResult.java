package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorCodeElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorsElement;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

/**
 *
 * @author zhanhb
 */
@Builder(builderClassName = "Builder")
@Value
@SuppressWarnings("FinalClass")
public class ErrorListResult {

  /**
   *
   * errors list.
   */
  @Singular
  List<ErrorElement> errors;
  ErrorCode errorCode;

  /**
   * add all error nodes from saved list to xml.
   *
   * @param rootElement XML root element
   */
  public void addErrorsTo(ConnectorElement.Builder rootElement) {
    ErrorCode error = errorCode;
    int code = error != null ? error.getCode() : 0;
    rootElement.error(
            ErrorCodeElement.builder().number(code).build()
    );
    List<ErrorElement> errorElements = errors;
    if (!errorElements.isEmpty()) {
      rootElement.errors(ErrorsElement.builder().errors(errorElements).build());
    }
  }

  @SuppressWarnings("PublicInnerClass")
  public static class Builder {

    /**
     * save errors node to list.
     *
     * @param fileItem the file who leads the error
     * @param errorCode error code
     */
    @SuppressWarnings("UnusedReturnValue")
    public Builder appendError(FileItem fileItem, ErrorCode errorCode) {
      int code = errorCode.getCode();
      String name = fileItem.getName();
      String folder = fileItem.getFolder();
      String type = fileItem.getType().getName();
      return error(ErrorElement.builder().type(type).name(name).folder(folder).code(code).build());
    }

    /**
     * checks if error list contains errors.
     *
     * @return true if there are any errors.
     */
    public ErrorListResult ifError(ErrorCode code) {
      return (errors != null && !errors.isEmpty() ? errorCode(code) : this).build();
    }
  }

}
