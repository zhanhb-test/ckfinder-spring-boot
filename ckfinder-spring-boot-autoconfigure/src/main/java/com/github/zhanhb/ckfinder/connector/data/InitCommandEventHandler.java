package com.github.zhanhb.ckfinder.connector.data;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;

/**
 *
 * @author zhanhb
 */
public interface InitCommandEventHandler extends IEventHandler<InitCommandEventArgs> {

  @Override
  public void runEventHandler(InitCommandEventArgs param, IConfiguration configuration);

}
