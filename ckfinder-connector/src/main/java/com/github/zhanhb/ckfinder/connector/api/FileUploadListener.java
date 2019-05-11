package com.github.zhanhb.ckfinder.connector.api;

import java.util.EventListener;

/**
 * Listener when file upload completed.
 *
 * @author zhanhb
 */
public interface FileUploadListener extends EventListener {

  void onFileUploadComplete(FileUploadEvent event);

}
