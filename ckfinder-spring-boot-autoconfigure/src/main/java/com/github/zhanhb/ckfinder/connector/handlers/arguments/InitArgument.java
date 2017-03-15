package com.github.zhanhb.ckfinder.connector.handlers.arguments;

import javax.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class InitArgument extends Arguments {

  private HttpServletRequest request;

}
