package com.github.zhanhb.ckfinder.connector.handlers.arguments;

import com.github.zhanhb.ckfinder.connector.handlers.response.DetailError;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class ErrorListXMLArguments extends XMLArguments {

  private int errorNum;

  /**
   *
   * errors list.
   */
  private final List<DetailError> errorList = new ArrayList<>(4);

}
