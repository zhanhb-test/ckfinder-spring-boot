package com.github.zhanhb.ckfinder.connector.data;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;

/**
 *
 * @author zhanhb
 */
public interface PluginInfoRegister {

  public void runEventHandler(InitCommandEvent event, IConfiguration configuration);

}
