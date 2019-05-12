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
import com.github.zhanhb.ckfinder.connector.handlers.parameter.RenameFileParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.RenamedFileElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.ErrorListResult;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Class to handle <code>RenameFile</code> command.
 */
@Slf4j
public class RenameFileCommand extends FailAtEndXmlCommand<RenameFileParameter> implements IPostCommand {

  /**
   * rename thumb file.
   *
   * @param param the parameter
   */
  private void renameThumb(RenameFileParameter param, CommandContext cmdContext) {
    cmdContext.resolveThumbnail(param.getFileName()).ifPresent(thumbFile -> {
      cmdContext.resolveThumbnail(param.getNewFileName()).ifPresent(newThumbFile -> {
        try {
          log.debug("remove thumb '{}'->'{}'", thumbFile, newThumbFile);
          Files.move(thumbFile, newThumbFile);
        } catch (IOException ignored) {
        }
      });
    });
  }

  /**
   * gets data for XML and checks all validation.
   *
   * @param param the parameter
   * @param cmdContext command context
   * @return error code or null if it's correct.
   * @throws ConnectorException when error occurs
   */
  @Override
  protected void createXml(RenameFileParameter param, CommandContext cmdContext, ConnectorElement.Builder rootElement)
          throws ConnectorException {
    log.trace("getDataForXml");
    CKFinderContext context = cmdContext.getCfCtx();
    cmdContext.checkType();
    cmdContext.checkAllPermission(AccessControl.FILE_RENAME);
    
    String fileName = param.getFileName();
    String newFileName = param.getNewFileName();
    
    if (context.isForceAscii()) {
      newFileName = FileUtils.convertToAscii(newFileName);
      param.setNewFileName(newFileName);
    }
    
    ErrorListResult.Builder builder = ErrorListResult.builder();
    
    boolean addExtraNode = false;
    if (!StringUtils.isEmpty(fileName) && !StringUtils.isEmpty(newFileName)) {
      addExtraNode = true;
    }
    
    ErrorListResult result;
    if (!FileUtils.isFileExtensionAllowed(newFileName, cmdContext.getType())) {
      result = builder.errorCode(ErrorCode.INVALID_EXTENSION).build();
    } else {
      if (!context.isDoubleFileExtensionsAllowed()) {
        newFileName = FileUtils.renameFileWithBadExt(cmdContext.getType(), newFileName);
        param.setNewFileName(newFileName);
      }
      
      if (!FileUtils.isFileNameValid(fileName) || context.isFileHidden(fileName)) {
        result = builder.errorCode(ErrorCode.INVALID_REQUEST).build();
      } else if (!FileUtils.isFileNameValid(newFileName, context) || context.isFileHidden(newFileName)) {
        result = builder.errorCode(ErrorCode.INVALID_NAME).build();
      } else if (!FileUtils.isFileExtensionAllowed(fileName, cmdContext.getType())) {
        result = builder.errorCode(ErrorCode.INVALID_REQUEST).build();
      } else {
        
        Path file = cmdContext.resolve(fileName);
        Path newFile = cmdContext.resolve(newFileName);
        
        try {
          Files.move(file, newFile);
          param.setRenamed(true);
          renameThumb(param, cmdContext);
          result = builder.build();
        } catch (NoSuchFileException ex) {
          result = builder.errorCode(ErrorCode.FILE_NOT_FOUND).build();
        } catch (FileAlreadyExistsException ex) {
          result = builder.errorCode(ErrorCode.ALREADY_EXIST).build();
        } catch (IOException ex) {
          param.setRenamed(false);
          log.error("IOException", ex);
          result = builder.errorCode(ErrorCode.ACCESS_DENIED).build();
        }
      }
    }
    result.addErrorsTo(rootElement);
    if (addExtraNode) {
      RenamedFileElement.Builder element = RenamedFileElement.builder().name(param.getFileName());
      if (param.isRenamed()) {
        element.newName(param.getNewFileName());
      }
      rootElement.result(element.build());
    }
  }
  
  @Override
  protected RenameFileParameter parseParameters(HttpServletRequest request, CKFinderContext context) {
    RenameFileParameter param = new RenameFileParameter();
    param.setFileName(request.getParameter("fileName"));
    param.setNewFileName(request.getParameter("newFileName"));
    return param;
  }
  
}
