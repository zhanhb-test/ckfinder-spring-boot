package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.utils.MessageUtil;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class FileUploadParameter {

  /**
   * Uploading file name request.
   */
  private String fileName;
  /**
   * File name after rename.
   */
  private String newFileName;
  /**
   * Function number to call after file upload is completed.
   */
  private String ckEditorFuncNum;
  /**
   * The selected response type to be used after file upload is completed.
   */
  private String responseType;
  /**
   * Function number to call after file upload is completed.
   */
  private String ckFinderFuncNum;
  /**
   * Language (locale) code.
   */
  private String langCode;
  /**
   * Error code number.
   */
  private ErrorCode errorCode;

  public FileUploadParameter() {
    this.fileName = "";
    this.newFileName = "";
  }

  public ConnectorException toException(ErrorCode code) {
    String msg = MessageUtil.INSTANCE.getMessage(getLangCode(), code.getCode());
    return new ConnectorException(code, msg);
  }

  public ConnectorException toException(String message) {
    Objects.requireNonNull(message);
    return new ConnectorException(ErrorCode.CUSTOM_ERROR, message);
  }

}
