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

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.data.AfterFileUploadEventArgs;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.errors.ErrorUtils;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>FileUpload</code> command.
 *
 */
@Slf4j
public class FileUploadCommand extends Command<FileUploadParameter> implements IPostCommand {

  /**
   * Array containing unsafe characters which can't be used in file name.
   */
  private static final Pattern UNSAFE_FILE_NAME_PATTERN = Pattern.compile("[:*?|/]");

  /**
   * default constructor.
   */
  public FileUploadCommand() {
    super(FileUploadParameter::new);
  }

  /**
   * Executes file upload command.
   *
   * @throws ConnectorException when error occurs.
   */
  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(FileUploadParameter param, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration) throws ConnectorException {
    try {
      String errorMsg = param.getErrorCode() == Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE ? "" : (param.getErrorCode() == Constants.Errors.CKFINDER_CONNECTOR_ERROR_CUSTOM_ERROR ? param.getCustomErrorMsg()
              : ErrorUtils.INSTANCE.getErrorMsgByLangAndCode(param.getLangCode(), param.getErrorCode()));
      errorMsg = errorMsg.replace("%1", param.getNewFileName());
      String path = "";

      if (!param.isUploaded()) {
        param.setNewFileName("");
        param.setCurrentFolder("");
      } else {
        path = param.getType().getUrl() + param.getCurrentFolder();
      }
      setContentType(param, response);
      PrintWriter writer = response.getWriter();
      if ("txt".equals(param.getResponseType())) {
        writer.write(param.getNewFileName() + "|" + errorMsg);
      } else if (checkFuncNum(param)) {
        handleOnUploadCompleteCallFuncResponse(writer, errorMsg, path, param, configuration);
      } else {
        handleOnUploadCompleteResponse(writer, errorMsg, param, configuration);
      }
      writer.flush();
    } catch (IOException | SecurityException e) {
      throw new ConnectorException(
              Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED, e);
    }
  }

  /**
   * check if func num is set in request.
   *
   * @param param
   * @return true if is.
   */
  protected boolean checkFuncNum(FileUploadParameter param) {
    return param.getCkFinderFuncNum() != null;
  }

  /**
   * return response when func num is set.
   *
   * @param out response.
   * @param errorMsg error message
   * @param path path
   * @param param
   * @param configuration connector configuration
   * @throws IOException when error occurs.
   */
  protected void handleOnUploadCompleteCallFuncResponse(Writer out, String errorMsg, String path, FileUploadParameter param, IConfiguration configuration) throws IOException {
    param.setCkFinderFuncNum(param.getCkFinderFuncNum().replaceAll("[^\\d]", ""));
    out.write("<script type=\"text/javascript\">");
    out.write("window.parent.CKFinder.tools.callFunction("
            + param.getCkFinderFuncNum() + ", '"
            + path
            + FileUtils.backupWithBackSlash(param.getNewFileName(), "'")
            + "', '" + errorMsg + "');");
    out.write("</script>");
  }

  /**
   *
   * @param writer out put stream
   * @param errorMsg error message
   * @param param
   * @param configuration connector configuration
   * @throws IOException when error occurs
   */
  protected void handleOnUploadCompleteResponse(Writer writer, String errorMsg, FileUploadParameter param, IConfiguration configuration) throws IOException {
    writer.write("<script type=\"text/javascript\">");
    writer.write("window.parent.OnUploadCompleted(");
    writer.write("'" + FileUtils.backupWithBackSlash(param.getNewFileName(), "'") + "'");
    writer.write(", '"
            + (param.getErrorCode()
            != Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE ? errorMsg
                    : "") + "'");
    writer.write(");");
    writer.write("</script>");
  }

  /**
   * initializing parametrs for command handler.
   *
   * @param param
   * @param request request
   * @param configuration connector configuration.
   */
  @Override
  protected void initParams(FileUploadParameter param, HttpServletRequest request, IConfiguration configuration) {
    try {
      super.initParams(param, request, configuration);
    } catch (ConnectorException ex) {
      param.setErrorCode(ex.getErrorCode());
    }
    param.setCkFinderFuncNum(request.getParameter("CKFinderFuncNum"));
    param.setCkEditorFuncNum(request.getParameter("CKEditorFuncNum"));
    param.setResponseType(request.getParameter("response_type") != null ? request.getParameter("response_type") : request.getParameter("responseType"));
    param.setLangCode(request.getParameter("langCode"));

    if (param.getErrorCode() == Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE) {
      param.setUploaded(uploadFile(request, param, configuration));
    }

  }

  /**
   * uploads file and saves to file.
   *
   * @param request request
   * @return true if uploaded correctly.
   */
  private boolean uploadFile(HttpServletRequest request, FileUploadParameter param, IConfiguration configuration) {
    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD)) {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UNAUTHORIZED);
      return false;
    }
    return fileUpload(request, param, configuration);
  }

  /**
   *
   * @param request http request
   * @return true if uploaded correctly
   */
  private boolean fileUpload(HttpServletRequest request, FileUploadParameter param, IConfiguration configuration) {
    try {
      Collection<Part> parts = request.getParts();
      for (Part part : parts) {
        String path = Paths.get(param.getType().getPath(),
                param.getCurrentFolder()).toString();
        param.setFileName(getFileItemName(part));
        if (validateUploadItem(part, path, param, configuration)) {
          return saveTemporaryFile(path, part, param, configuration);
        }
      }
      return false;
    } catch (ConnectorException e) {
      param.setErrorCode(e.getErrorCode());
      if (param.getErrorCode() == Constants.Errors.CKFINDER_CONNECTOR_ERROR_CUSTOM_ERROR) {
        param.setCustomErrorMsg(e.getMessage());
      }
      return false;
    } catch (IOException | ServletException | RuntimeException e) {
      String message = e.getMessage();
      if (message != null
              && (message.toLowerCase().contains("sizelimit")
              || message.contains("size limit"))) {
        log.info("", e);
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG);
        return false;
      }
      log.error("", e);
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
      return false;
    }

  }

  /**
   * saves temporary file in the correct file path.
   *
   * @param path path to save file
   * @param item file upload item
   * @return result of saving, true if saved correctly
   * @throws Exception when error occurs.
   */
  private boolean saveTemporaryFile(String path, Part item, FileUploadParameter param, IConfiguration configuration)
          throws IOException, ConnectorException {
    Path file = Paths.get(path, param.getNewFileName());

    if (!ImageUtils.isImageExtension(file)) {
      item.write(file.toString());
      AfterFileUploadEventArgs args = new AfterFileUploadEventArgs(param.getCurrentFolder(), file);
      configuration.getEvents().runAfterFileUpload(args, configuration);
      return true;
    } else if (ImageUtils.checkImageSize(item, configuration)
            || configuration.isCheckSizeAfterScaling()) {
      ImageUtils.createTmpThumb(item, file, getFileItemName(item), configuration);
      if (!configuration.isCheckSizeAfterScaling()
              || FileUtils.isFileSizeInRange(param.getType(), Files.size(file))) {
        AfterFileUploadEventArgs args = new AfterFileUploadEventArgs(param.getCurrentFolder(), file);
        configuration.getEvents().runAfterFileUpload(args, configuration);
        return true;
      } else {
        Files.deleteIfExists(file);
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG);
        return false;
      }
    } else {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG);
      return false;
    }
  }

  /**
   * if file exists this method adds (number) to file.
   *
   * @param path folder
   * @param name file name
   * @return new file name.
   */
  private String getFinalFileName(String path, String name, FileUploadParameter param) {
    Path file = Paths.get(path, name);
    int number = 0;

    String nameWithoutExtension = FileUtils.getFileNameWithoutExtension(name, false);

    if (Files.exists(file) || isProtectedName(nameWithoutExtension)) {
      @SuppressWarnings("StringBufferWithoutInitialCapacity")
      StringBuilder sb = new StringBuilder();
      sb.append(FileUtils.getFileNameWithoutExtension(name, false)).append("(");
      int len = sb.length();
      do {
        number++;
        sb.append(number).append(").").append(FileUtils.getFileExtension(name, false));
        param.setNewFileName(sb.toString());
        sb.setLength(len);
        file = Paths.get(path, param.getNewFileName());
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_FILE_RENAMED);
      } while (Files.exists(file));
    }
    return param.getNewFileName();
  }

  /**
   * validates uploaded file.
   *
   * @param item uploaded item.
   * @param path file path
   * @return true if validation
   */
  private boolean validateUploadItem(Part item, String path, FileUploadParameter param, IConfiguration configuration) {

    if (item.getSubmittedFileName() != null && item.getSubmittedFileName().length() > 0) {
      param.setFileName(getFileItemName(item));
    } else {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_INVALID);
      return false;
    }
    param.setNewFileName(param.getFileName());

    param.setNewFileName(UNSAFE_FILE_NAME_PATTERN.matcher(param.getNewFileName()).replaceAll("_"));

    if (configuration.isDisallowUnsafeCharacters()) {
      param.setNewFileName(param.getNewFileName().replace(';', '_'));
    }
    if (configuration.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToASCII(param.getNewFileName()));
    }
    if (!param.getNewFileName().equals(param.getFileName())) {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_INVALID_NAME_RENAMED);
    }

    if (FileUtils.isDirectoryHidden(param.getCurrentFolder(), configuration)) {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_REQUEST);
      return false;
    }
    if (!FileUtils.isFileNameInvalid(param.getNewFileName())
            || FileUtils.isFileHidden(param.getNewFileName(), configuration)) {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_NAME);
      return false;
    }
    final ResourceType resourceType = param.getType();
    if (!FileUtils.isFileExtensionAllwed(param.getNewFileName(), resourceType)) {
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_INVALID_EXTENSION);
      return false;
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(resourceType, param.getNewFileName()));
    }

    try {
      Path file = Paths.get(path, getFinalFileName(path, param.getNewFileName(), param));
      if (!(ImageUtils.isImageExtension(file) && configuration.isCheckSizeAfterScaling())
              && !FileUtils.isFileSizeInRange(resourceType, item.getSize())) {
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_TOO_BIG);
        return false;
      }

      if (configuration.isSecureImageUploads() && ImageUtils.isImageExtension(file)
              && !ImageUtils.isValid(item)) {
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_CORRUPT);
        return false;
      }

      if (!FileUtils.isExtensionHtml(file.getFileName().toString(), configuration)
              && FileUtils.hasHtmlContent(item)) {
        param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_UPLOADED_WRONG_HTML_FILE);
        return false;
      }
    } catch (SecurityException | IOException e) {
      log.error("", e);
      param.setErrorCode(Constants.Errors.CKFINDER_CONNECTOR_ERROR_ACCESS_DENIED);
      return false;
    }

    return true;
  }

  /**
   * save if uploaded file item name is full file path not only file name.
   *
   * @param item file upload item
   * @return file name of uploaded item
   */
  private String getFileItemName(Part item) {
    Pattern p = Pattern.compile("[^\\\\/]+$");
    Matcher m = p.matcher(item.getSubmittedFileName());

    return (m.find()) ? m.group(0) : "";
  }

  @Deprecated
  @Override
  protected boolean isCurrFolderExists(FileUploadParameter param, HttpServletRequest request, IConfiguration configuration) {
    try {
      String tmpType = request.getParameter("type");
      if (tmpType != null) {
        try {
          checkTypeExists(tmpType, configuration);
        } catch (ConnectorException ex) {
          param.setErrorCode(ex.getErrorCode());
          return false;
        }
        Path currDir = Paths.get(configuration.getTypes().get(tmpType).getPath(),
                param.getCurrentFolder());
        if (!Files.isDirectory(currDir)) {
          throw new ConnectorException(
                  Constants.Errors.CKFINDER_CONNECTOR_ERROR_FOLDER_NOT_FOUND);
        } else {
          return true;
        }
      }
      return true;
    } catch (ConnectorException ex) {
      param.setErrorCode(ex.getErrorCode());
      return false;
    }
  }

  private boolean isProtectedName(String nameWithoutExtension) {
    return Pattern.compile("^(AUX|COM\\d|CLOCK\\$|CON|NUL|PRN|LPT\\d)$",
            Pattern.CASE_INSENSITIVE).matcher(nameWithoutExtension).matches();
  }

  void setContentType(FileUploadParameter param, HttpServletResponse response) {
    response.setContentType("text/html;charset=UTF-8");
  }

}
