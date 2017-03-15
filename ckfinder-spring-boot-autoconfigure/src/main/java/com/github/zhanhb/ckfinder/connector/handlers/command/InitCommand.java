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
import com.github.zhanhb.ckfinder.connector.configuration.License;
import com.github.zhanhb.ckfinder.connector.data.InitCommandEventArgs;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.InitParameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorInfo;
import com.github.zhanhb.ckfinder.connector.handlers.response.ResourceTypes;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.FileUtils;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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
  private static final int LICENSE_KEY_LENGTH = 34;
  private static final char[] hexChars = "0123456789abcdef".toCharArray();

  public InitCommand() {
    super(InitParameter::new);
  }

  @Override
  Connector buildConnector(InitParameter param, IConfiguration configuration) {
    Connector.Builder rootElement = Connector.builder();
    if (param.getType() != null) {
      rootElement.resourceType(param.getType().getName());
    }
    createErrorNode(rootElement, 0);
    createConnectorData(rootElement, param, configuration);
    try {
      createResouceTypesData(rootElement, param, configuration);
    } catch (Exception e) {
      log.error("", e);
    }
    createPluginsData(rootElement, configuration);
    return rootElement.build();
  }

  /**
   * Creates connector node in XML.
   *
   * @param rootElement root element in XML
   */
  private void createConnectorData(Connector.Builder rootElement, InitParameter param, IConfiguration configuration) {
    // connector info
    ConnectorInfo.Builder element = ConnectorInfo.builder();
    element.enabled(configuration.isEnabled());
    License license = configuration.getLicense(param.getRequest());
    element.licenseName(getLicenseName(license));
    element.licenseKey(createLicenseKey(license.getKey()));
    element.thumbsEnabled(configuration.isThumbsEnabled());
    element.uploadCheckImages(!configuration.isCheckSizeAfterScaling());
    if (configuration.isThumbsEnabled()) {
      element.thumbsUrl(PathUtils.addSlashToEnd(configuration.getThumbsUrl()));
      element.thumbsDirectAccess(configuration.isThumbsDirectAccess());
      element.thumbsWidth(configuration.getMaxThumbWidth());
      element.thumbsHeight(configuration.getMaxThumbHeight());
    }
    element.imgWidth(configuration.getImgWidth());
    element.imgHeight(configuration.getImgHeight());
    String plugins = getPlugins(configuration);
    if (plugins.length() > 0) {
      element.plugins(plugins);
    }
    rootElement.connectorInfo(element.build());
  }

  /**
   * gets plugins names.
   *
   * @return plugins names.
   */
  private String getPlugins(IConfiguration configuration) {
    return configuration.getPublicPluginNames();
  }

  /**
   * checks license key.
   *
   * @return license name if key is ok, or empty string if not.
   */
  private String getLicenseName(License license) {
    if (validateLicenseKey(license.getKey())) {
      int index = Constants.CKFINDER_CHARS.indexOf(license.getKey().charAt(0))
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
    return licenseKey != null && licenseKey.length() == LICENSE_KEY_LENGTH;
  }

  /**
   * Creates plugins node in XML.
   *
   * @param rootElement root element in XML
   */
  private void createPluginsData(Connector.Builder rootElement, IConfiguration configuration) {
    if (configuration.getEvents() != null) {
      InitCommandEventArgs args = new InitCommandEventArgs(rootElement);
      configuration.getEvents().runInitCommand(args, configuration);
    }
  }

  /**
   * Creates plugins node in XML.
   *
   * @param rootElement root element in XML
   * @throws Exception when error occurs
   */
  @SuppressWarnings("CollectionWithoutInitialCapacity")
  private void createResouceTypesData(Connector.Builder rootElement, InitParameter param, IConfiguration configuration) throws IOException {
    //resurcetypes
    ResourceTypes.Builder resourceTypes = ResourceTypes.builder();
    Collection<ResourceType> types;
    if (param.getType() != null) {
      types = Collections.singleton(param.getType());
    } else {
      types = getTypes(configuration);
    }

    for (ResourceType resourceType : types) {
      String key = resourceType.getName();
      if (configuration.getAccessControl().hasPermission(key, "/", param.getUserRole(),
              AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_VIEW)) {
        com.github.zhanhb.ckfinder.connector.handlers.response.ResourceType.Builder childElement = com.github.zhanhb.ckfinder.connector.handlers.response.ResourceType.builder();
        childElement.name(resourceType.getName());
        childElement.acl(configuration.getAccessControl().getAcl(key, "/", param.getUserRole()));
        childElement.hash(randomHash(resourceType.getPath()));
        childElement.allowedExtensions(resourceType.getAllowedExtensions());
        childElement.deniedExtensions(resourceType.getDeniedExtensions());
        childElement.url(PathUtils.addSlashToEnd(resourceType.getUrl()));
        long maxSize = resourceType.getMaxSize();
        childElement.maxSize(maxSize > 0 ? maxSize : 0);
        boolean hasChildren = FileUtils.hasChildren(configuration.getAccessControl(), "/", Paths.get(PathUtils.escape(resourceType.getPath())), configuration, resourceType.getName(), param.getUserRole());
        childElement.hasChildren(hasChildren);
        resourceTypes.resourceType(childElement.build());
      }
    }
    rootElement.resourceTypes(resourceTypes.build());
  }

  /**
   * gets list of types names.
   *
   * @return list of types names.
   */
  private Collection<ResourceType> getTypes(IConfiguration configuration) {
    if (configuration.getDefaultResourceTypes().size() > 0) {
      Set<String> defaultResourceTypes = configuration.getDefaultResourceTypes();
      ArrayList<ResourceType> arrayList = new ArrayList<>(defaultResourceTypes.size());
      for (String key : defaultResourceTypes) {
        ResourceType resourceType = configuration.getTypes().get(key);
        if (resourceType != null) {
          arrayList.add(resourceType);
        }
      }
      return arrayList;
    } else {
      return configuration.getTypes().values();
    }
  }

  /**
   * Gets hash for folders in XML response to avoid cached responses.
   *
   * @param folder folder
   * @return hash value
   */
  private String randomHash(String folder) {
    try {
      MessageDigest algorithm = MessageDigest.getInstance("SHA-256");
      byte[] messageDigest = algorithm.digest(folder.getBytes(StandardCharsets.UTF_8));
      int len = messageDigest.length;

      StringBuilder hexString = new StringBuilder(len << 1);

      for (int i = 0; i < len; i++) {
        byte b = messageDigest[i];
        hexString.append(hexChars[b >> 4 & 15]).append(hexChars[b & 15]);
      }
      return hexString.substring(0, 15);
    } catch (NoSuchAlgorithmException e) {
      log.error("", e);
      return "";
    }
  }

  @Override
  protected void initParams(InitParameter param, HttpServletRequest request, IConfiguration configuration)
          throws ConnectorException {
    super.initParams(param, request, configuration);
    param.setRequest(request);
  }

  @Deprecated
  @Override
  String setCurrentFolder(InitParameter param, HttpServletRequest request) {
    return null;
  }

}
