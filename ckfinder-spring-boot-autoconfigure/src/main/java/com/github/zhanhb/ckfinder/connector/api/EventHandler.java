package com.github.zhanhb.ckfinder.connector.api;

/**
 *
 * @author zhanhb
 */
public interface EventHandler {

  public void fireOnFileUpload(FileUploadEvent args) throws ConnectorException;

  public void fireOnInitCommand(InitCommandEvent event);

}
