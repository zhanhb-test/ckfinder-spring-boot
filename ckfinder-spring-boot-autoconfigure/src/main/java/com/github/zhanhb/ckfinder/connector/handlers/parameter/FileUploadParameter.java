package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.errors.ErrorUtils;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
public class FileUploadParameter extends Parameter {

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
   * Flag informing if file was uploaded correctly.
   */
  private boolean uploaded;
  /**
   * Error code number.
   */
  private ConnectorError errorCode;

  public FileUploadParameter() {
    this.fileName = "";
    this.newFileName = "";
  }

  @Override
  public void throwException(ConnectorError code) throws ConnectorException {
    String msg = ErrorUtils.INSTANCE.getErrorMsgByLangAndCode(getLangCode(), code.getCode());
    throw new ConnectorException(code, msg);
  }

  public void throwException(String message) throws ConnectorException {
    Objects.requireNonNull(message);
    throw new ConnectorException(ConnectorError.CUSTOM_ERROR, message);
  }

}
