/*
 * CKFinder
 * ========
 * http://cksource.com/ckfinder
 * Copyright (C) 2007-2015, CKSource - Frederico Knabben. All rights reserved.
 *
 * The software, this file and its contents are subject to the CKFinder
 * License. Please read the license.txt file before using, installing, copying,
 * modifying or distribute this file or part of its contents. The contents of
 * this file is part of the Source Code of CKFinder.
 */
package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

/**
 * Class to handle <code>QuickUpload</code> command.
 */
public class QuickUploadCommand extends FileUploadCommand {

  private final Gson gson = new GsonBuilder().serializeNulls().create();

  @Override
  protected void handleOnUploadCompleteResponse(Writer writer, String errorMsg, FileUploadParameter param) throws IOException {
    if ("json".equalsIgnoreCase(param.getResponseType())) {
      handleJSONResponse(writer, errorMsg, null, param);
    } else {
      ConnectorError errorCode = param.getErrorCode();
      int errorNum = errorCode != null ? errorCode.getCode() : 0;
      writer.write("<script type=\"text/javascript\">window.parent.OnUploadCompleted(" + errorNum + ", ");
      if (param.isUploaded()) {
        writer.write("'" + param.getType().getUrl()
                + param.getCurrentFolder()
                + FileUtils.escapeJavaScript(FileUtils.encodeURIComponent(param.getNewFileName()))
                + "', '" + FileUtils.escapeJavaScript(param.getNewFileName())
                + "', ");
      } else {
        writer.write("'', '', ");
      }
      writer.write("'');</script>");
    }
  }

  @Override
  protected void handleOnUploadCompleteCallFuncResponse(Writer writer, String errorMsg, String path, FileUploadParameter param) throws IOException {
    if ("json".equalsIgnoreCase(param.getResponseType())) {
      handleJSONResponse(writer, errorMsg, path, param);
    } else {
      param.setCkEditorFuncNum(param.getCkEditorFuncNum().replaceAll("\\D", ""));
      writer.write("<script type=\"text/javascript\">window.parent.CKEDITOR.tools.callFunction(" + param.getCkEditorFuncNum() + ", '"
              + path
              + FileUtils.escapeJavaScript(FileUtils.encodeURIComponent(param.getNewFileName()))
              + "', '" + errorMsg + "');</script>");
    }
  }

  @Override
  protected boolean checkFuncNum(FileUploadParameter param) {
    return param.getCkEditorFuncNum() != null;
  }

  @Override
  void setContentType(FileUploadParameter param, HttpServletResponse response) {
    if ("json".equalsIgnoreCase(param.getResponseType())) {
      response.setContentType("application/json;charset=UTF-8");
    } else {
      response.setContentType("text/html;charset=UTF-8");
    }
  }

  /**
   * Writes JSON object into response stream after uploading file which was
   * dragged and dropped in to CKEditor 4.5 or higher.
   *
   * @param writer the response stream
   * @param errorMsg string representing error message which indicates that
   * there was an error during upload or uploaded file was renamed
   * @param path path to uploaded file
   * @param param
   * @throws java.io.IOException
   */
  private void handleJSONResponse(Writer writer, String errorMsg, String path, FileUploadParameter param) throws IOException {
    Map<String, Object> jsonObj = new HashMap<>(6);

    jsonObj.put("fileName", param.getNewFileName());
    jsonObj.put("uploaded", param.isUploaded() ? 1 : 0);

    if (param.isUploaded()) {
      if (path != null && !path.isEmpty()) {
        jsonObj.put("url", path + FileUtils.escapeJavaScript(FileUtils.encodeURIComponent(param.getNewFileName())));
      } else {
        jsonObj.put("url",
                param.getType().getUrl()
                + param.getCurrentFolder()
                + FileUtils.escapeJavaScript(FileUtils
                        .encodeURIComponent(param.getNewFileName())));
      }
    }

    if (errorMsg != null && !errorMsg.isEmpty()) {
      Map<String, Object> jsonErrObj = new HashMap<>(3);
      ConnectorError error = param.getErrorCode();
      jsonErrObj.put("number", error != null ? error.getCode() : 0);
      jsonErrObj.put("message", errorMsg);
      jsonObj.put("error", jsonErrObj);
    }

    writer.write(gson.toJson(jsonObj));
  }

}
