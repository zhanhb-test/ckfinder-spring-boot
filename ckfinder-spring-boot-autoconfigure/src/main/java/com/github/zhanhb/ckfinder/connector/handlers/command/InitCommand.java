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
import com.github.zhanhb.ckfinder.connector.api.InitCommandEvent;
import com.github.zhanhb.ckfinder.connector.api.License;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.api.ThumbnailProperties;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.InitParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorInfo;
import com.github.zhanhb.ckfinder.connector.handlers.response.PluginsInfos;
import com.github.zhanhb.ckfinder.connector.handlers.response.ResourceTypes;
import com.github.zhanhb.ckfinder.connector.support.KeyGenerator;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Class to handle <code>Init</code> command.
 */
@Slf4j
public class InitCommand extends XmlCommand<InitParameter> {

  /**
   * chars taken to license key.
   */
  private static final int[] LICENSE_CHARS = {11, 0, 8, 12, 26, 2, 3, 25, 1};
  private static final int LICENSE_CHAR_NR = 5;
  private static final int MIN_LICENSE_KEY_LENGTH = 26;
  private static final char[] hexChars = "0123456789abcdef".toCharArray();

  @Override
  Connector buildConnector(InitParameter param, CKFinderContext context) {
    Connector.Builder rootElement = Connector.builder();
    if (param.getType() != null) {
      rootElement.resourceType(param.getType().getName());
    }
    createErrorNode(rootElement, 0);
    createConnectorData(rootElement, param, context);
    createResouceTypesData(rootElement, param, context);
    createPluginsData(rootElement, context);
    return rootElement.build();
  }

  /**
   * Creates connector node in XML.
   *
   * @param rootElement root element in XML
   * @param param the parameter
   * @param context connector context
   */
  private void createConnectorData(Connector.Builder rootElement, InitParameter param, CKFinderContext context) {
    ThumbnailProperties thumbnail = context.getThumbnail();
    License license = context.getLicense(param.getHost());

    // connector info
    ConnectorInfo.Builder element = ConnectorInfo.builder()
            .enabled(context.isEnabled())
            .licenseName(getLicenseName(license))
            .licenseKey(createLicenseKey(license.getKey()))
            .uploadCheckImages(!context.isCheckSizeAfterScaling())
            .imgWidth(context.getImage().getMaxWidth())
            .imgHeight(context.getImage().getMaxHeight())
            .thumbsEnabled(thumbnail != null)
            .plugins(context.getPublicPluginNames());
    if (thumbnail != null) {
      element.thumbsUrl(PathUtils.addSlashToEnd(thumbnail.getUrl()))
              .thumbsDirectAccess(thumbnail.isDirectAccess())
              .thumbsWidth(thumbnail.getMaxWidth())
              .thumbsHeight(thumbnail.getMaxHeight());
    }
    rootElement.result(element.build());
  }

  /**
   * checks license key.
   *
   * @param license
   * @return license name if key is ok, or empty string if not.
   */
  private String getLicenseName(License license) {
    if (validateLicenseKey(license.getKey())) {
      int index = KeyGenerator.INSTANCE.indexOf(license.getKey().charAt(0))
              % LICENSE_CHAR_NR;
      if (index == 1 || index == 4) {
        return license.getName();
      }
    }
    return "";
  }

  /**
   * Creates license key from key in configuration.
   *
   * @param licenseKey license key from configuration
   * @return hashed license key
   */
  private String createLicenseKey(String licenseKey) {
    if (validateLicenseKey(licenseKey)) {
      StringBuilder sb = new StringBuilder(LICENSE_CHARS.length);
      for (int i : LICENSE_CHARS) {
        sb.append(licenseKey.charAt(i));
      }
      return sb.toString();
    }
    return "";
  }

  /**
   * validates license key length.
   *
   * @param licenseKey config license key
   * @return true if has correct length
   */
  private boolean validateLicenseKey(String licenseKey) {
    return licenseKey != null && licenseKey.length() >= MIN_LICENSE_KEY_LENGTH;
  }

  /**
   * Creates plugins node in XML.
   *
   * @param rootElement root element in XML
   * @param context ckfinder context
   */
  private void createPluginsData(Connector.Builder rootElement, CKFinderContext context) {
    PluginsInfos.Builder builder = PluginsInfos.builder();
    InitCommandEvent event = new InitCommandEvent(builder);
    context.fireOnInitCommand(event);
    rootElement.result(builder.build());
  }

  /**
   * Creates plugins node in XML.
   *
   * @param rootElement root element in XML
   * @param param the parameter
   * @param context ckfinder context
   */
  private void createResouceTypesData(Connector.Builder rootElement, InitParameter param, CKFinderContext context) {
    //resurcetypes
    ResourceTypes.Builder resourceTypes = ResourceTypes.builder();
    Collection<ResourceType> types;
    if (param.getType() != null) {
      types = Collections.singleton(param.getType());
    } else {
      types = getTypes(context);
    }

    for (ResourceType resourceType : types) {
      String name = resourceType.getName();
      int acl = context.getAccessControl().getAcl(name, "/", param.getUserRole());
      if ((acl & AccessControl.FOLDER_VIEW) != 0) {
        long maxSize = resourceType.getMaxSize();
        boolean hasChildren = FileUtils.hasChildren(context.getAccessControl(), "/", getPath(resourceType.getPath()), context, resourceType.getName(), param.getUserRole());
        resourceTypes.resourceType(com.github.zhanhb.ckfinder.connector.handlers.response.ResourceType.builder()
                .name(name)
                .acl(acl)
                .hash(randomHash(resourceType.getPath()))
                .allowedExtensions(resourceType.getAllowedExtensions())
                .deniedExtensions(resourceType.getDeniedExtensions())
                .url(PathUtils.addSlashToEnd(resourceType.getUrl()))
                .maxSize(maxSize > 0 ? maxSize : 0)
                .hasChildren(hasChildren).build());
      }
    }
    rootElement.result(resourceTypes.build());
  }

  /**
   * gets list of types names.
   *
   * @param context ckfinder context
   * @return list of types names.
   */
  private Collection<ResourceType> getTypes(CKFinderContext context) {
    if (context.getDefaultResourceTypes().size() > 0) {
      Set<String> defaultResourceTypes = context.getDefaultResourceTypes();
      ArrayList<ResourceType> arrayList = new ArrayList<>(defaultResourceTypes.size());
      for (String key : defaultResourceTypes) {
        ResourceType resourceType = context.getTypes().get(key);
        if (resourceType != null) {
          arrayList.add(resourceType);
        }
      }
      return arrayList;
    } else {
      return context.getTypes().values();
    }
  }

  /**
   * Gets hash for folders in XML response to avoid cached responses.
   *
   * @param folder folder
   * @return hash value
   */
  private String randomHash(Path folder) {
    try {
      MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
      byte[] bytes = algorithm.digest(folder.toString().getBytes(StandardCharsets.UTF_8));
      int len = bytes.length;

      StringBuilder hexString = new StringBuilder(len << 1);

      for (byte b : bytes) {
        hexString.append(hexChars[b >> 4 & 15]).append(hexChars[b & 15]);
      }
      return hexString.substring(0, 15);
    } catch (NoSuchAlgorithmException e) {
      log.error("", e);
      return "";
    }
  }

  @Override
  protected InitParameter popupParams(HttpServletRequest request, CKFinderContext context)
          throws ConnectorException {
    InitParameter param = doInitParam(new InitParameter(), request, context);
    param.setHost(request.getServerName());
    return param;
  }

  @Deprecated
  @Override
  String getCurrentFolder(HttpServletRequest request) {
    return null;
  }

}
