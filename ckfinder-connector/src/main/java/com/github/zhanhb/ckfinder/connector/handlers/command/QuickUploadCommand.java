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

import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.unbescape.json.JsonEscape;

/**
 * Class to handle <code>QuickUpload</code> command.
 */
public class QuickUploadCommand extends FileUploadCommand {

  @Override
  protected void finish(HttpServletResponse response, @Nonnull String uri,
          FileUploadParameter param, String errorMsg) throws IOException {
    final String name = param.getNewFileName();
    final String responseType = param.getResponseType();
    final String ckEditorFuncNum = param.getCkEditorFuncNum();
    final ErrorCode errorCode = param.getErrorCode();
    final int errorNum = errorCode != null ? errorCode.getCode() : 0;

    response.setCharacterEncoding("UTF-8");
    if ("json".equalsIgnoreCase(responseType)) {
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      try (PrintWriter writer = response.getWriter()) {
        writeJSON(writer, errorMsg, uri, name, errorNum);
      }
    } else if ("txt".equalsIgnoreCase(responseType)) {
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      try (PrintWriter writer = response.getWriter()) {
        writer.write(name + "|" + errorMsg);
      }
    } else {
      response.setContentType(MediaType.TEXT_HTML_VALUE);
      try (PrintWriter writer = response.getWriter()) {
        if (ckEditorFuncNum != null) {
          writer.write("<script>//<![CDATA[\nwindow.parent.CKEDITOR.tools.callFunction("
                  + ckEditorFuncNum.replaceAll("\\D", "") + ", '"
                  + FileUtils.escapeJavaScript(uri)
                  + "', '" + errorMsg + "')//]]></script>");
        } else {
          boolean success = StringUtils.hasLength(uri);
          writer.write("<script>//<![CDATA[\nwindow.parent.OnUploadCompleted(" + errorNum + ", '");
          if (success) {
            writer.write(FileUtils.escapeJavaScript(uri) + "', '"
                    + FileUtils.escapeJavaScript(name));
          } else {
            writer.write("', '");
          }
          writer.write("', '')//]]></script>");
        }
      }
    }
  }

  // for test
  void writeJSON(Writer writer, String errorMsg, String uri,
          String fileName, int errorNum) throws IOException {
    boolean success = StringUtils.hasLength(uri);

    writer.write("{\"fileName\":");
    if (fileName != null) {
      writer.write('"');
      JsonEscape.escapeJsonMinimal(fileName, writer);
      writer.write('"');
    } else {
      writer.write("null");
    }
    writer.write(",\"uploaded\":");
    writer.write(success ? '1' : '0');
    if (StringUtils.hasLength(errorMsg)) {
      writer.write(",\"error\":{\"number\":");
      writer.write(Integer.toString(errorNum));
      writer.write(",\"message\":\"");
      JsonEscape.escapeJsonMinimal(errorMsg, writer);
      writer.write("\"}");
    }
    if (success) {
      assert fileName != null;
      writer.write(",\"url\":\"");
      JsonEscape.escapeJsonMinimal(uri, writer);
      writer.write('"');
    }
    writer.write('}');
  }

}
