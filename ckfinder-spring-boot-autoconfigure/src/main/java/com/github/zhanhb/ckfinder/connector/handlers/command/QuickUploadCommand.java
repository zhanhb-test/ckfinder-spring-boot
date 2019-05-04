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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.util.StringUtils;

/**
 * Class to handle <code>QuickUpload</code> command.
 */
public class QuickUploadCommand extends FileUploadCommand {

  @Override
  protected void handleOnUploadCompleteResponse(Writer writer, String errorMsg, FileUploadParameter param, String path) throws IOException {
    if ("json".equalsIgnoreCase(param.getResponseType())) {
      handleJSONResponse(writer, errorMsg, path, param);
    } else {
      ErrorCode errorCode = param.getErrorCode();
      int errorNum = errorCode != null ? errorCode.getCode() : 0;
      boolean success = !StringUtils.isEmpty(path);
      writer.write("<script>//<![CDATA[\nwindow.parent.OnUploadCompleted(" + errorNum + ", '");
      if (success) {
        String name = param.getNewFileName();
        String uri = path + FileUtils.encodeURIComponent(name);
        writer.write(FileUtils.escapeJavaScript(uri) + "', '"
                + FileUtils.escapeJavaScript(name));
      } else {
        writer.write("', '");
      }
      writer.write("', '')//]]></script>");
    }
  }

  @Override
  protected void handleOnUploadCompleteCallFuncResponse(Writer writer, String errorMsg, String path, FileUploadParameter param) throws IOException {
    if ("json".equalsIgnoreCase(param.getResponseType())) {
      handleJSONResponse(writer, errorMsg, path, param);
    } else {
      param.setCkEditorFuncNum(param.getCkEditorFuncNum().replaceAll("\\D", ""));
      writer.write("<script>//<![CDATA[\nwindow.parent.CKEDITOR.tools.callFunction("
              + param.getCkEditorFuncNum() + ", '"
              + FileUtils.escapeJavaScript(path + FileUtils.encodeURIComponent(param.getNewFileName()))
              + "', '" + errorMsg + "')//]]></script>");
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
   * @param param the parameter
   * @throws IOException when IO Exception occurs.
   */
  private void handleJSONResponse(Writer writer, String errorMsg, String path,
          FileUploadParameter param) throws IOException {
    Map<String, Object> jsonObj = new HashMap<>(4);

    boolean success = !StringUtils.isEmpty(path);
    String fileName = param.getNewFileName();
    jsonObj.put("fileName", fileName);
    jsonObj.put("uploaded", success ? 1 : 0);

    if (success) {
      jsonObj.put("url", path + FileUtils.encodeURIComponent(fileName));
    }

    if (!StringUtils.isEmpty(errorMsg)) {
      Map<String, Object> jsonErrObj = new HashMap<>(3);
      ErrorCode error = param.getErrorCode();
      jsonErrObj.put("number", error != null ? error.getCode() : 0);
      jsonErrObj.put("message", errorMsg);
      jsonObj.put("error", jsonErrObj);
    }

    ObjectMapperHolder.MAPPER.writeValue(writer, jsonObj);
  }

  private interface ObjectMapperHolder {

    ObjectMapper MAPPER = new Jackson2ObjectMapperBuilder().serializationInclusion(JsonInclude.Include.ALWAYS).build();

  }

}
