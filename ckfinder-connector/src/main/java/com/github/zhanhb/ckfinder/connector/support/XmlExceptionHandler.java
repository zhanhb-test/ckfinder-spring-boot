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
package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.ExceptionHandler;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolderElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorCodeElement;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

/**
 * Class to handle errors from commands returning XML response.
 */
public enum XmlExceptionHandler implements ExceptionHandler {

  INSTANCE;

  @Override
  public void handleException(HttpServletRequest request,
          HttpServletResponse response, CKFinderContext context,
          ConnectorException connectorException) throws IOException {
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(context.getUserRoleName());

    ConnectorElement.Builder connector = ConnectorElement.builder();
    String currentFolder = connectorException.getCurrentFolder();
    ResourceType type = connectorException.getType();

    int errorNum = connectorException.getErrorCode().getCode();

    if (type != null) {
      connector.resourceType(type.getName());
      if (StringUtils.hasLength(currentFolder)) {
        AccessControl ac = context.getAccessControl();
        connector.currentFolder(CurrentFolderElement.builder()
                .path(currentFolder)
                .url(type.getUrl() + currentFolder)
                .acl(ac.getAcl(type.getName(), currentFolder, userRole))
                .build());
      }
    }
    connector.error(ErrorCodeElement.builder()
            .number(errorNum)
            .value(connectorException.getMessage()).build());
    String result = XmlCreator.INSTANCE.toString(connector.build());

    response.setContentType(MediaType.APPLICATION_XML_VALUE);
    response.setCharacterEncoding("utf-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      out.write(result);
    }
  }

}
