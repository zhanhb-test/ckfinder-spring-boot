package com.github.zhanhb.ckfinder.connector.api;

/**
 *
 * @author zhanhb
 */
public interface EventHandler {

  void fireOnFileUpload(FileUploadEvent event);

  void fireOnInitCommand(InitPluginInfo data);

}
