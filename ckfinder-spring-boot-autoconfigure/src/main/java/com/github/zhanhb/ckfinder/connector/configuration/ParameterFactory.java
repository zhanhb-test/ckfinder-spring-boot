package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;

/**
 *
 * @author zhanhb
 * @param <T>
 */
public interface ParameterFactory<T extends Parameter> {

  T newParameter();

}
