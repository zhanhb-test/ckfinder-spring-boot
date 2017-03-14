package com.github.zhanhb.ckfinder.connector.data;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;

/**
 *
 * @author zhanhb
 */
public interface AfterFileUploadEventHandler extends IEventHandler<AfterFileUploadEventArgs> {

  @Override
  public boolean runEventHandler(AfterFileUploadEventArgs args, IConfiguration configuration) throws ConnectorException;

}
