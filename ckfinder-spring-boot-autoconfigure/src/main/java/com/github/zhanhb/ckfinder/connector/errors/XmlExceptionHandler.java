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
package com.github.zhanhb.ckfinder.connector.errors;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import com.github.zhanhb.ckfinder.connector.utils.XmlCreator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.util.StringUtils;

/**
 * Class to handle errors from commands returning XML response.
 */
public enum XmlExceptionHandler implements ExceptionHandler {

  INSTANCE;

  @Override
  public void handleException(HttpServletRequest request,
          HttpServletResponse response, IConfiguration configuration,
          ConnectorException connectorException) throws IOException {
    HttpSession session = request.getSession(false);
    String userRole = session == null ? null : (String) session.getAttribute(configuration.getUserRoleName());

    Connector.Builder connector = Connector.builder();
    String currentFolder = connectorException.getCurrentFolder();
    ResourceType type = connectorException.getType();

    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    int errorNum = connectorException.getErrorCode().getCode();

    if (type != null) {
      String typeName = type.getName();
      connector.resourceType(typeName);
      if (StringUtils.hasLength(currentFolder)) {
        connector.currentFolder(CurrentFolder.builder()
                .path(currentFolder)
                .url(type.getUrl() + currentFolder)
                .acl(configuration.getAccessControl().getAcl(typeName, currentFolder, userRole))
                .build());
      }
    }
    connector.error(Error.builder()
            .number(errorNum)
            .value(connectorException.getMessage()).build());
    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    String result = XmlCreator.INSTANCE.toString(connector.build());
    
    try (PrintWriter out = response.getWriter()) {
      out.write(result);
    }
  }

}
