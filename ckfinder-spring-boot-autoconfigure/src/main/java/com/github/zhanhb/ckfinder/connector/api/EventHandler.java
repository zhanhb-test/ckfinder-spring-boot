package com.github.zhanhb.ckfinder.connector.api;

/**
 *
 * @author zhanhb
 */
public interface EventHandler {

  void fireOnFileUpload(FileUploadEvent args) throws ConnectorException;

  void fireOnInitCommand(InitCommandEvent event);

}
