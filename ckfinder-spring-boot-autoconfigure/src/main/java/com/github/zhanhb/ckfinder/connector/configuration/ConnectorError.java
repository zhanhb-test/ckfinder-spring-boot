package com.github.zhanhb.ckfinder.connector.configuration;

/**
 * Class holding CKFinder error codes.
 */
public enum ConnectorError {

  CUSTOM_ERROR(1),
  INVALID_COMMAND(10),
  TYPE_NOT_SPECIFIED(11),
  INVALID_TYPE(12),
  INVALID_NAME(102),
  UNAUTHORIZED(103),
  ACCESS_DENIED(104),
  INVALID_EXTENSION(105),
  INVALID_REQUEST(109),
  @Deprecated
  UNKNOWN(110),
  ALREADY_EXIST(115),
  FOLDER_NOT_FOUND(116),
  FILE_NOT_FOUND(117),
  SOURCE_AND_TARGET_PATH_EQUAL(118),
  UPLOADED_FILE_RENAMED(201),
  UPLOADED_INVALID(202),
  UPLOADED_TOO_BIG(203),
  UPLOADED_CORRUPT(204),
  UPLOADED_NO_TMP_DIR(205),
  UPLOADED_WRONG_HTML_FILE(206),
  UPLOADED_INVALID_NAME_RENAMED(207),
  MOVE_FAILED(300),
  COPY_FAILED(301),
  DELETE_FAILED(302),
  CONNECTOR_DISABLED(500),
  THUMBNAILS_DISABLED(501);

  private final int code;

  ConnectorError(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

}
