package com.github.zhanhb.ckfinder.connector.data;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import java.util.EventListener;

/**
 *
 * @author zhanhb
 */
public interface FileUploadListener extends EventListener {

  public void onFileUploadComplete(FileUploadEvent event, IConfiguration configuration) throws ConnectorException;

}
