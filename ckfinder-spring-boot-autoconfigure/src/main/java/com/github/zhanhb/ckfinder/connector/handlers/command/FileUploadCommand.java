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

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.FileUploadEvent;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * Class to handle <code>FileUpload</code> command.
 *
 */
@Slf4j
public class FileUploadCommand extends BaseCommand<FileUploadParameter> implements IPostCommand {

  /**
   * Array containing unsafe characters which can't be used in file name.
   */
  private static final Pattern UNSAFE_FILE_NAME_PATTERN = Pattern.compile("[:*?|/]");

  /**
   * Executes file upload command.
   *
   * @param param the parameter
   * @param request request
   * @param response
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs.
   * @throws IOException when IO Exception occurs.
   */
  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(FileUploadParameter param, HttpServletRequest request,
          HttpServletResponse response, Configuration configuration) throws ConnectorException, IOException {
    String errorMsg = "";
    try {
      checkParam(param); // set in method initParams
      uploadFile(request, param, configuration);
      param.setUploaded(true);
      checkParam(param); // set in method uploadFile
    } catch (ConnectorException ex) {
      param.setErrorCode(ex.getErrorCode());
      errorMsg = ex.getMessage();
    }
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
      handleOnUploadCompleteCallFuncResponse(writer, errorMsg, path, param);
    } else {
      handleOnUploadCompleteResponse(writer, errorMsg, param);
    }
    writer.flush();
  }

  /**
   * check if func num is set in request.
   *
   * @param param the parameter
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
   * @param param the parameter
   * @throws IOException when IO Exception occurs.
   */
  protected void handleOnUploadCompleteCallFuncResponse(Writer out, String errorMsg, String path, FileUploadParameter param) throws IOException {
    param.setCkFinderFuncNum(param.getCkFinderFuncNum().replaceAll("[^\\d]", ""));
    out.write("<script type=\"text/javascript\">window.parent.CKFinder.tools.callFunction("
            + param.getCkFinderFuncNum() + ", '"
            + path
            + FileUtils.escapeJavaScript(param.getNewFileName())
            + "', '" + errorMsg + "');</script>");
  }

  /**
   *
   * @param writer out put stream
   * @param errorMsg error message
   * @param param the parameter
   * @throws IOException when IO Exception occurs.
   */
  protected void handleOnUploadCompleteResponse(Writer writer, String errorMsg, FileUploadParameter param) throws IOException {
    writer.write("<script type=\"text/javascript\">window.parent.OnUploadCompleted('" + FileUtils.escapeJavaScript(param.getNewFileName()) + "', '"
            + (param.getErrorCode()
            != null ? errorMsg
                    : "") + "');</script>");
  }

  /**
   * initializing parameters for command handler.
   *
   * @param request request
   * @param configuration connector configuration.
   * @return the parameter
   */
  @Override
  protected FileUploadParameter popupParams(HttpServletRequest request, Configuration configuration) {
    FileUploadParameter param = new FileUploadParameter();
    try {
      doInitParam(param, request, configuration);
    } catch (ConnectorException ex) {
      param.setErrorCode(ex.getErrorCode());
    }
    param.setCkFinderFuncNum(request.getParameter("CKFinderFuncNum"));
    param.setCkEditorFuncNum(request.getParameter("CKEditorFuncNum"));
    param.setResponseType(request.getParameter("response_type") != null ? request.getParameter("response_type") : request.getParameter("responseType"));
    param.setLangCode(request.getParameter("langCode"));
    if (param.getType() == null) {
      param.setErrorCode(ConnectorError.INVALID_TYPE);
    }
    return param;
  }

  /**
   * uploads file and saves to file.
   *
   * @param request request
   * @param param the parameter
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs
   */
  private void uploadFile(HttpServletRequest request, FileUploadParameter param,
          Configuration configuration) throws ConnectorException {
    if (!configuration.getAccessControl().hasPermission(param.getType().getName(),
            param.getCurrentFolder(), param.getUserRole(),
            AccessControl.FILE_UPLOAD)) {
      param.throwException(ConnectorError.UNAUTHORIZED);
    }
    fileUpload(request, param, configuration);
  }

  /**
   *
   * @param request http request
   * @param param the parameter
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs
   */
  private void fileUpload(HttpServletRequest request, FileUploadParameter param,
          Configuration configuration) throws ConnectorException {
    MultipartResolver multipartResolver = StandardHolder.RESOLVER;
    try {
      boolean multipart = multipartResolver.isMultipart(request);
      if (multipart) {
        log.debug("prepare resolveMultipart");
        MultipartHttpServletRequest resolveMultipart = multipartResolver.resolveMultipart(request);
        log.debug("finish resolveMultipart");
        try {
          Collection<MultipartFile> parts = resolveMultipart.getFileMap().values();
          for (MultipartFile part : parts) {
            Path path = getPath(param.getType().getPath(),
                    param.getCurrentFolder());
            param.setFileName(getFileItemName(part));
            validateUploadItem(part, path, param, configuration);
            saveTemporaryFile(path, part, param, configuration);
            return;
          }
        } finally {
          multipartResolver.cleanupMultipart(resolveMultipart);
        }
      }
      param.throwException("No file provided in the request.");
    } catch (MultipartException e) {
      log.debug("catch MultipartException", e);
      param.throwException(ConnectorError.UPLOADED_TOO_BIG);
    } catch (IOException e) {
      log.debug("catch IOException", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  /**
   * saves temporary file in the correct file path.
   *
   * @param path path to save file
   * @param item file upload item
   * @param param the parameter
   * @param configuration connector configuration
   * @throws IOException when IO Exception occurs.
   * @throws ConnectorException when error occurs
   */
  private void saveTemporaryFile(Path path, MultipartFile item, FileUploadParameter param, Configuration configuration)
          throws IOException, ConnectorException {
    Path file = getPath(path, param.getNewFileName());

    if (ImageUtils.isImageExtension(file)) {
      if (!configuration.isCheckSizeAfterScaling()
              && !ImageUtils.checkImageSize(item, configuration)) {
        param.throwException(ConnectorError.UPLOADED_TOO_BIG);
      }
      ImageUtils.createTmpThumb(item, file, getFileItemName(item), configuration);
      if (configuration.isCheckSizeAfterScaling()
              && !FileUtils.isFileSizeInRange(param.getType(), Files.size(file))) {
        Files.deleteIfExists(file);
        param.throwException(ConnectorError.UPLOADED_TOO_BIG);
      }
    } else {
      try (InputStream in = item.getInputStream()) {
        Files.copy(in, file);
      }
    }
    FileUploadEvent args = new FileUploadEvent(param.getCurrentFolder(), file);
    configuration.getEvents().fireOnFileUpload(args);
  }

  /**
   * if file exists this method adds (number) to file.
   *
   * @param path folder
   * @param param the parameter
   * @return new file name.
   */
  private String getFinalFileName(Path path, FileUploadParameter param) {
    String name = param.getNewFileName();
    Path file = getPath(path, name);

    String nameWithoutExtension = FileUtils.getNameWithoutLongExtension(name);

    if (Files.exists(file) || isProtectedName(nameWithoutExtension)) {
      @SuppressWarnings("StringBufferWithoutInitialCapacity")
      StringBuilder sb = new StringBuilder(nameWithoutExtension).append("(");
      String suffix = ")." + FileUtils.getLongExtension(name);
      int len = sb.length();
      int number = 0;
      do {
        number++;
        sb.append(number).append(suffix);
        name = sb.toString();
        sb.setLength(len);
        file = getPath(path, name);
      } while (Files.exists(file));
      param.setErrorCode(ConnectorError.UPLOADED_FILE_RENAMED);
      param.setNewFileName(name);
    }
    return name;
  }

  /**
   * validates uploaded file.
   *
   * @param item uploaded item.
   * @param path file path
   * @param param the parameter
   * @param configuration connector configuration
   * @throws ConnectorException when error occurs
   */
  private void validateUploadItem(MultipartFile item, Path path,
          FileUploadParameter param, Configuration configuration) throws ConnectorException {
    if (item.getOriginalFilename() == null || item.getOriginalFilename().length() <= 0) {
      param.throwException(ConnectorError.UPLOADED_INVALID);
    }
    param.setFileName(getFileItemName(item));
    param.setNewFileName(param.getFileName());

    param.setNewFileName(UNSAFE_FILE_NAME_PATTERN.matcher(param.getNewFileName()).replaceAll("_"));

    if (configuration.isDisallowUnsafeCharacters()) {
      param.setNewFileName(param.getNewFileName().replace(';', '_'));
    }
    if (configuration.isForceAscii()) {
      param.setNewFileName(FileUtils.convertToAscii(param.getNewFileName()));
    }
    if (!param.getNewFileName().equals(param.getFileName())) {
      param.setErrorCode(ConnectorError.UPLOADED_INVALID_NAME_RENAMED);
    }

    if (configuration.isDirectoryHidden(param.getCurrentFolder())) {
      param.throwException(ConnectorError.INVALID_REQUEST);
    }
    if (!FileUtils.isFileNameValid(param.getNewFileName())
            || configuration.isFileHidden(param.getNewFileName())) {
      param.throwException(ConnectorError.INVALID_NAME);
    }
    final ResourceType resourceType = param.getType();
    if (!FileUtils.isFileExtensionAllowed(param.getNewFileName(), resourceType)) {
      param.throwException(ConnectorError.INVALID_EXTENSION);
    }
    if (configuration.isCheckDoubleFileExtensions()) {
      param.setNewFileName(FileUtils.renameFileWithBadExt(resourceType, param.getNewFileName()));
    }

    try {
      Path file = getPath(path, getFinalFileName(path, param));
      if ((!ImageUtils.isImageExtension(file) || !configuration.isCheckSizeAfterScaling())
              && !FileUtils.isFileSizeInRange(resourceType, item.getSize())) {
        param.throwException(ConnectorError.UPLOADED_TOO_BIG);
      }

      if (configuration.isSecureImageUploads() && ImageUtils.isImageExtension(file)
              && !ImageUtils.isValid(item)) {
        param.throwException(ConnectorError.UPLOADED_CORRUPT);
      }

      if (!FileUtils.isExtensionHtml(file.getFileName().toString(), configuration)
              && FileUtils.hasHtmlContent(item)) {
        param.throwException(ConnectorError.UPLOADED_WRONG_HTML_FILE);
      }
    } catch (IOException e) {
      log.error("", e);
      param.throwException(ConnectorError.ACCESS_DENIED);
    }
  }

  /**
   * save if uploaded file item name is full file path not only file name.
   *
   * @param item file upload item
   * @return file name of uploaded item
   */
  private String getFileItemName(MultipartFile item) {
    Pattern p = Pattern.compile("[^\\\\/]+$");
    Matcher m = p.matcher(item.getOriginalFilename());
    return m.find() ? m.group() : "";
  }

  private boolean isProtectedName(String nameWithoutExtension) {
    return Pattern.compile("^(AUX|COM\\d|CLOCK\\$|CON|NUL|PRN|LPT\\d)$",
            Pattern.CASE_INSENSITIVE).matcher(nameWithoutExtension).matches();
  }

  void setContentType(FileUploadParameter param, HttpServletResponse response) {
    response.setContentType("text/html;charset=UTF-8");
  }

  private void checkParam(FileUploadParameter param) throws ConnectorException {
    ConnectorError code = param.getErrorCode();
    if (code != null) {
      param.throwException(code);
    }
  }

  @SuppressWarnings("UtilityClassWithoutPrivateConstructor")
  private static class StandardHolder {

    static final MultipartResolver RESOLVER = new StandardServletMultipartResolver();

  }

}
