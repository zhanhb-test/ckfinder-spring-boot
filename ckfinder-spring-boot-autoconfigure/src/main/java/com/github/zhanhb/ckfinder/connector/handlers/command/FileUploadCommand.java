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
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.FileUploadEvent;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.FileUploadParameter;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.ImageUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
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
   * @param response response
   * @param context ckfinder context
   * @throws IOException when IO Exception occurs.
   */
  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(FileUploadParameter param, HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context) throws IOException {
    String errorMsg = "";
    String path = "";

    boolean uploaded = false;
    try {
      CommandContext cmdContext = populateCommandContext(request, context);
      cmdContext.checkType();
      uploadFile(request, param, cmdContext);
      path = cmdContext.getType().getUrl() + cmdContext.getCurrentFolder();
      uploaded = true;
      checkParam(param);
    } catch (ConnectorException ex) {
      log.info("got ConnectorException", ex);
      param.setErrorCode(ex.getErrorCode());
      errorMsg = ex.getMessage();
      if (!uploaded) {
        param.setNewFileName("");
      }
    }
    finish(response, path, param, errorMsg.replace("%1", param.getNewFileName()));
  }

  /**
   * initializing parameters for command handler.
   *
   * @param request request
   * @param context ckfinder context.
   * @return the parameter
   */
  @Override
  protected FileUploadParameter popupParams(HttpServletRequest request, CKFinderContext context) {
    FileUploadParameter param = new FileUploadParameter();
    param.setCkFinderFuncNum(request.getParameter("CKFinderFuncNum"));
    param.setCkEditorFuncNum(request.getParameter("CKEditorFuncNum"));
    String responseType = request.getParameter("response_type");
    if (responseType == null) {
      responseType = request.getParameter("responseType");
    }
    param.setResponseType(responseType);
    param.setLangCode(request.getParameter("langCode"));
    return param;
  }

  /**
   * uploads file and saves to file.
   *
   * @param request request
   * @param param the parameter
   * @param cmdContext command context
   * @throws ConnectorException when error occurs
   */
  private void uploadFile(HttpServletRequest request, FileUploadParameter param,
          CommandContext cmdContext) throws ConnectorException {
    cmdContext.checkAllPermission(AccessControl.FILE_UPLOAD);
    fileUpload(request, param, cmdContext);
  }

  /**
   *
   * @param request http request
   * @param param the parameter
   * @param cmdContext command context
   * @throws ConnectorException when error occurs
   */
  private void fileUpload(HttpServletRequest request, FileUploadParameter param,
          CommandContext cmdContext) throws ConnectorException {
    MultipartResolver multipartResolver = StandardHolder.RESOLVER;
    try {
      boolean multipart = multipartResolver.isMultipart(request);
      if (multipart) {
        log.debug("prepare resolveMultipart");
        MultipartHttpServletRequest resolveMultipart = multipartResolver.resolveMultipart(request);
        log.debug("finish resolveMultipart");
        try {
          Collection<MultipartFile> parts = resolveMultipart.getFileMap().values();
          //noinspection LoopStatementThatDoesntLoop
          for (MultipartFile part : parts) {
            param.setFileName(getFileItemName(part));
            validateUploadItem(part, param, cmdContext);
            saveTemporaryFile(part, param, cmdContext);
            return;
          }
        } finally {
          multipartResolver.cleanupMultipart(resolveMultipart);
        }
      }
      param.throwException("No file provided in the request.");
    } catch (MultipartException e) {
      log.debug("catch MultipartException", e);
      param.throwException(ErrorCode.UPLOADED_TOO_BIG);
    } catch (IOException e) {
      log.debug("catch IOException", e);
      param.throwException(ErrorCode.ACCESS_DENIED);
    }
  }

  /**
   * saves temporary file in the correct file path.
   *
   * @param item file upload item
   * @param param the parameter
   * @param cmdContext command context
   * @throws IOException when IO Exception occurs.
   * @throws ConnectorException when error occurs
   */
  private void saveTemporaryFile(MultipartFile item,
          FileUploadParameter param, CommandContext cmdContext)
          throws IOException, ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    Path file = cmdContext.resolve(param.getNewFileName());

    if (ImageUtils.isImageExtension(file)) {
      if (!context.isCheckSizeAfterScaling()
              && !ImageUtils.checkImageSize(item, context)) {
        param.throwException(ErrorCode.UPLOADED_TOO_BIG);
      }
      ImageUtils.createTmpThumb(item, file, context);
      if (context.isCheckSizeAfterScaling()
              && FileUtils.isFileSizeOutOfRange(cmdContext.getType(), Files.size(file))) {
        Files.deleteIfExists(file);
        param.throwException(ErrorCode.UPLOADED_TOO_BIG);
      }
    } else {
      try (InputStream in = item.getInputStream()) {
        Files.copy(in, file);
      }
    }
    context.fireOnFileUpload(new FileUploadEvent(this, cmdContext.getCurrentFolder(), file));
  }

  /**
   * if file exists this method adds (number) to file.
   *
   * @param cmdContext command context
   * @param param the parameter
   * @return new file name.
   */
  private String getFinalFileName(CommandContext cmdContext, FileUploadParameter param) {
    String name = param.getNewFileName();
    Path file = cmdContext.resolve(name);

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
        file = cmdContext.resolve(name);
      } while (Files.exists(file));
      param.setErrorCode(ErrorCode.UPLOADED_FILE_RENAMED);
      param.setNewFileName(name);
    }
    return name;
  }

  /**
   * validates uploaded file.
   *
   * @param item uploaded item.
   * @param param the parameter
   * @param cmdContext command context
   * @throws ConnectorException when error occurs
   */
  private void validateUploadItem(MultipartFile item,
          FileUploadParameter param, CommandContext cmdContext) throws ConnectorException {
    CKFinderContext context = cmdContext.getCfCtx();
    if (StringUtils.isEmpty(item.getOriginalFilename())) {
      param.throwException(ErrorCode.UPLOADED_INVALID);
    }

    String fileName = param.getFileName();
    String newFileName = UNSAFE_FILE_NAME_PATTERN.matcher(fileName).replaceAll("_");
    if (context.isDisallowUnsafeCharacters()) {
      newFileName = newFileName.replace(';', '_');
    }
    if (context.isForceAscii()) {
      newFileName = FileUtils.convertToAscii(newFileName);
    }
    if (!newFileName.equals(fileName)) {
      param.setErrorCode(ErrorCode.UPLOADED_INVALID_NAME_RENAMED);
    }

    if (context.isDirectoryHidden(cmdContext.getCurrentFolder())) {
      param.throwException(ErrorCode.INVALID_REQUEST);
    }
    if (!FileUtils.isFileNameValid(newFileName)
            || context.isFileHidden(newFileName)) {
      param.throwException(ErrorCode.INVALID_NAME);
    }
    final ResourceType resourceType = cmdContext.getType();
    if (!FileUtils.isFileExtensionAllowed(newFileName, resourceType)) {
      param.throwException(ErrorCode.INVALID_EXTENSION);
    }
    if (!context.isDoubleFileExtensionsAllowed()) {
      newFileName = FileUtils.renameFileWithBadExt(resourceType, newFileName);
    }

    param.setNewFileName(newFileName);
    Path file = cmdContext.resolve(getFinalFileName(cmdContext, param));
    if ((!ImageUtils.isImageExtension(file) || !context.isCheckSizeAfterScaling())
            && FileUtils.isFileSizeOutOfRange(resourceType, item.getSize())) {
      param.throwException(ErrorCode.UPLOADED_TOO_BIG);
    }

    if (context.isSecureImageUploads() && ImageUtils.isImageExtension(file)
            && !ImageUtils.isValid(item)) {
      param.throwException(ErrorCode.UPLOADED_CORRUPT);
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
    String filename = item.getOriginalFilename();
    if (filename != null) {
      Matcher m = p.matcher(filename);
      if (m.find()) {
        return m.group();
      }
    }
    return "";
  }

  private boolean isProtectedName(@Nonnull String nameWithoutExtension) {
    return Pattern.compile("^(AUX|COM\\d|CLOCK\\$|CON|NUL|PRN|LPT\\d)$",
            Pattern.CASE_INSENSITIVE).matcher(nameWithoutExtension).matches();
  }

  private void checkParam(FileUploadParameter param) throws ConnectorException {
    ErrorCode code = param.getErrorCode();
    if (code != null) {
      param.throwException(code);
    }
  }

  protected void finish(HttpServletResponse response, @Nonnull String path,
          FileUploadParameter param, String errorMsg) throws IOException {

    String name = param.getNewFileName();
    String ckFinderFuncNum = param.getCkFinderFuncNum();
    String responseType = param.getResponseType();
    ErrorCode errorCode = param.getErrorCode();

    String contentType, result;

    if ("txt".equalsIgnoreCase(responseType)) {
      contentType = "text/plain;charset=UTF-8";
      result = name + "|" + errorMsg;
    } else {
      contentType = "text/html;charset=UTF-8";
      if (ckFinderFuncNum != null) {
        String uri = path + FileUtils.encodeURIComponent(name);
        result = "<script>//<![CDATA[\nwindow.parent.CKFinder.tools.callFunction("
                + ckFinderFuncNum.replaceAll("[^\\d]", "") + ", '"
                + FileUtils.escapeJavaScript(uri)
                + "', '" + errorMsg + "')//]]></script>";
      } else {
        result = "<script>//<![CDATA[\nwindow.parent.OnUploadCompleted('"
                + FileUtils.escapeJavaScript(name) + "', '"
                + (errorCode != null ? errorMsg : "")
                + "')//]]></script>";
      }
    }

    response.setContentType(contentType);
    try (PrintWriter writer = response.getWriter()) {
      writer.write(result);
    }
  }

  private interface StandardHolder {

    MultipartResolver RESOLVER = new StandardServletMultipartResolver();

  }

}
