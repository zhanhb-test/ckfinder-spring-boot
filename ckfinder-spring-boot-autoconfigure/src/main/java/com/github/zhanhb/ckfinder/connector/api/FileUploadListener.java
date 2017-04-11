package com.github.zhanhb.ckfinder.connector.api;

import java.util.EventListener;

/**
 *
 * @author zhanhb
 */
public interface FileUploadListener extends EventListener {

  public void onFileUploadComplete(FileUploadEvent event) throws ConnectorException;

}
