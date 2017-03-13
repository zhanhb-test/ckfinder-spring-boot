package com.github.zhanhb.ckfinder.connector.handlers.arguments;

import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class XMLArguments extends Arguments {

  private final Connector.Builder connector = Connector.builder();

}
