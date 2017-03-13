/*
 * Copyright 2016 ZJNU ACM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.DefaultPathBuilder;
import com.github.zhanhb.ckfinder.connector.configuration.FixLicenseFactory;
import com.github.zhanhb.ckfinder.connector.configuration.IBasePathBuilder;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.configuration.KeyGenerator;
import com.github.zhanhb.ckfinder.connector.configuration.License;
import com.github.zhanhb.ckfinder.connector.configuration.Plugin;
import com.github.zhanhb.ckfinder.connector.data.AccessControlLevel;
import com.github.zhanhb.ckfinder.connector.data.PluginInfo;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.plugins.FileEditor;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResize;
import com.github.zhanhb.ckfinder.connector.plugins.Watermark;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkSettings;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 *
 * @author zhanhb
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(CKFinderProperties.class)
@SuppressWarnings("PublicInnerClass")
public class CKFinderAutoConfiguration {

  @Configuration
  @ConditionalOnMissingBean(IBasePathBuilder.class)
  public static class DefaultBasePathBuilderConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public DefaultPathBuilder defaultPathBuilder() {
      ServletContext servletContext = applicationContext.getBean(ServletContext.class);
      String baseDir = servletContext.getRealPath(IConfiguration.DEFAULT_BASE_URL);
      return DefaultPathBuilder.builder()
              .baseDir(baseDir)
              .baseUrl(IConfiguration.DEFAULT_BASE_URL)
              .build();
    }
  }

  @Configuration
  @ConditionalOnMissingBean(AccessControl.class)
  public static class DefaultAccessControlConfiguration {

    private static int calc(int old, boolean condition, int mask) {
      return condition ? old | mask : old & ~mask;
    }

    @Autowired
    private CKFinderProperties properties;

    @Bean
    public AccessControl defaultAccessControl() {
      AccessControl accessControl = new AccessControl();
      CKFinderProperties.AccessControl[] accessControls = properties.getAccessControls();
      if (accessControls != null) {
        for (CKFinderProperties.AccessControl ac : accessControls) {
          String role = ac.getRole();
          String resourceType = ac.getResourceType();
          String folder = ac.getFolder();
          int mask = 0;
          mask = calc(mask, ac.isFileDelete(), AccessControl.CKFINDER_CONNECTOR_ACL_FILE_DELETE);
          mask = calc(mask, ac.isFileRename(), AccessControl.CKFINDER_CONNECTOR_ACL_FILE_RENAME);
          mask = calc(mask, ac.isFileUpload(), AccessControl.CKFINDER_CONNECTOR_ACL_FILE_UPLOAD);
          mask = calc(mask, ac.isFileView(), AccessControl.CKFINDER_CONNECTOR_ACL_FILE_VIEW);
          mask = calc(mask, ac.isFolderCreate(), AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_CREATE);
          mask = calc(mask, ac.isFolderDelete(), AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_DELETE);
          mask = calc(mask, ac.isFolderRename(), AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_RENAME);
          mask = calc(mask, ac.isFolderView(), AccessControl.CKFINDER_CONNECTOR_ACL_FOLDER_VIEW);

          AccessControlLevel accessControlLevel = AccessControlLevel
                  .builder().role(role).resourceType(resourceType).folder(folder)
                  .mask(mask).build();
          accessControl.addPermission(accessControlLevel);
        }
      }
      return accessControl;
    }

  }

  @Configuration
  @ConditionalOnMissingBean(IConfiguration.class)
  public static class DefaultConfigurationConfiguration {

    private static String toString(String[] array) {
      return Arrays.stream(array).collect(Collectors.joining(","));
    }

    @Autowired
    private CKFinderProperties properties;
    @Autowired
    private IBasePathBuilder basePathBuilder;
    @Autowired
    private AccessControl defaultAccessControl;
    @Autowired(required = false)
    private final Collection<Plugin> plugins = Collections.emptyList();

    @Bean
    public IConfiguration configuration() throws IOException {
      com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder = com.github.zhanhb.ckfinder.connector.configuration.Configuration.builder();
      if (properties.getEnabled() != null) {
        builder.enabled(properties.getEnabled());
      }
      CKFinderProperties.License license = properties.getLicense();
      label:
      {
        License.Builder licenseBuilder = License.builder().name("").key("");
        if (license != null) {
          licenseBuilder.name(license.getName()).key(license.getKey());
          CKFinderProperties.LicenseStrategy strategy = license.getStrategy();
          if (strategy == CKFinderProperties.LicenseStrategy.host) {
            builder.licenseFactory(new HostLicenseFactory());
            break label;
          } else if (strategy == CKFinderProperties.LicenseStrategy.auth) {
            if (!StringUtils.isEmpty(license.getName()) && StringUtils.isEmpty(license.getKey())) {
              licenseBuilder.key(KeyGenerator.INSTANCE.generateKey(false, license.getName(), 34));
            }
          }
        }
        builder.licenseFactory(new FixLicenseFactory(licenseBuilder.build()));
      }
      CKFinderProperties.Image image = properties.getImage();
      if (image != null) {
        if (image.getWidth() != null) {
          builder.imgWidth(image.getWidth());
        }
        if (image.getHeight() != null) {
          builder.imgHeight(image.getHeight());
        }
        if (image.getQuality() != null) {
          builder.imgQuality(image.getQuality());
        }
      }
      if (properties.getDefaultResourceTypes() != null) {
        builder.defaultResourceTypes(Arrays.asList(properties.getDefaultResourceTypes()));
      }
      if (properties.getTypes() != null) {
        setTypes(builder);
      }
      if (properties.getUserRoleSessionVar() != null) {
        builder.userRoleName(properties.getUserRoleSessionVar());
      }
      builder.accessControl(defaultAccessControl);
      setThumbs(builder);
      if (properties.getDisallowUnsafeCharacters() != null) {
        builder.disallowUnsafeCharacters(properties.getDisallowUnsafeCharacters());
      }
      if (properties.getCheckDoubleExtension() != null) {
        builder.checkDoubleFileExtensions(properties.getCheckDoubleExtension());
      }
      if (properties.getCheckSizeAfterScaling() != null) {
        builder.checkSizeAfterScaling(properties.getCheckSizeAfterScaling());
      }
      if (properties.getSecureImageUploads() != null) {
        builder.secureImageUploads(properties.getSecureImageUploads());
      }
      if (properties.getHtmlExtensions() != null) {
        builder.htmlExtensions(Arrays.asList(properties.getHtmlExtensions()));
      }
      if (properties.getForceAscii() != null) {
        builder.forceAscii(properties.getForceAscii());
      }
      if (properties.getHideFolders() != null) {
        builder.hiddenFolders(Arrays.asList(properties.getHideFolders()));
      }
      if (properties.getHideFiles() != null) {
        builder.hiddenFiles(Arrays.asList(properties.getHideFiles()));
      }
      builder.eventsFromPlugins(plugins);
      return builder.build();
    }

    private void setTypes(com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder) throws IOException {
      String baseDir = basePathBuilder.getBaseDir();
      String baseUrl = basePathBuilder.getBaseUrl();
      for (Map.Entry<String, CKFinderProperties.Type> entry : properties.getTypes().entrySet()) {
        final String typeName = entry.getKey();
        CKFinderProperties.Type type = entry.getValue();
        Assert.hasText(typeName, "Resource type name should not be empty");
        ResourceType.Builder resourceTypeBuilder = ResourceType.builder();
        resourceTypeBuilder.name(typeName);

        if (type.getAllowedExtensions() != null) {
          resourceTypeBuilder.allowedExtensions(toString(type.getAllowedExtensions()));
        }
        if (type.getDeniedExtensions() != null) {
          resourceTypeBuilder.deniedExtensions(toString(type.getDeniedExtensions()));
        }
        if (type.getMaxSize() != null) {
          resourceTypeBuilder.maxSize(type.getMaxSize());
        }
        String path = !StringUtils.isEmpty(type.getDirectory())
                ? type.getDirectory()
                : Constants.BASE_DIR_PLACEHOLDER + "/" + typeName.toLowerCase() + "/";
        resourceTypeBuilder.path(Files.createDirectories(
                Paths.get(path
                        .replace(Constants.BASE_DIR_PLACEHOLDER, baseDir)))
                .toAbsolutePath().toString());
        String url = type.getUrl() != null ? type.getUrl() : Constants.BASE_URL_PLACEHOLDER + "/" + typeName.toLowerCase();
        resourceTypeBuilder.url(url.replace(Constants.BASE_URL_PLACEHOLDER, baseUrl));

        builder.type(typeName, resourceTypeBuilder.build());
      }
    }

    private void setThumbs(com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder) {
      CKFinderProperties.Thumbs thumbs = properties.getThumbs();
      if (thumbs != null) {
        String baseDir = basePathBuilder.getBaseDir();
        String baseUrl = basePathBuilder.getBaseUrl();
        if (thumbs.getEnabled() != null) {
          builder.thumbsEnabled(thumbs.getEnabled());
        }
        if (thumbs.getDirectory() != null) {
          String path = thumbs.getDirectory().replace(Constants.BASE_DIR_PLACEHOLDER, baseDir);
          builder.thumbsPath(path);
        }
        if (thumbs.getDirectAccess() != null) {
          builder.thumbsDirectAccess(thumbs.getDirectAccess());
        }
        if (thumbs.getUrl() != null) {
          builder.thumbsUrl(thumbs.getUrl().replace(Constants.BASE_URL_PLACEHOLDER, baseUrl));
        }
        if (thumbs.getMaxHeight() != null) {
          builder.maxThumbHeight(thumbs.getMaxHeight());
        }
        if (thumbs.getMaxWidth() != null) {
          builder.maxThumbWidth(thumbs.getMaxWidth());
        }
        if (thumbs.getQuality() != null) {
          builder.imgQuality(thumbs.getQuality());
        }
      }
    }

  }

  @Configuration
  @ConditionalOnMissingBean(FileEditor.class)
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".file-editor", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static class DefaultFileEditorConfiguration {

    @Bean
    public FileEditor fileEditor() {
      return new FileEditor();
    }

  }

  @Configuration
  @ConditionalOnMissingBean(ImageResize.class)
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".image-resize", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static class DefaultImageResizeConfiguration {

    @Autowired
    private CKFinderProperties properties;

    @Bean
    public ImageResize imageResize() {
      CKFinderProperties.ImageResize imageResize = properties.getImageResize();
      Map<String, String> params = imageResize.getParams();
      PluginInfo.Builder pluginInfoBuilder = PluginInfo.builder();
      if (params != null) {
        pluginInfoBuilder.params(params);
      }
      return new ImageResize(pluginInfoBuilder.build().getParams());
    }

  }

  @Configuration
  @ConditionalOnMissingBean(Watermark.class)
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".watermark", name = "enabled", havingValue = "true", matchIfMissing = false)
  public static class DefaultWatermarkConfiguration {

    @Autowired
    private CKFinderProperties properties;
    @Autowired
    private ResourceLoader resourceLoader;

    @Bean
    public Watermark watermark() {
      CKFinderProperties.Watermark watermark = properties.getWatermark();
      WatermarkSettings.Builder builder = WatermarkSettings.builder();
      String source = watermark.getSource();
      Assert.notNull(source, "waltermark source should not be null");
      Resource resource = resourceLoader.getResource(source);
      Assert.isTrue(resource.exists(), "waltermark resource not exists");
      if (watermark.getMarginBottom() != null) {
        builder.marginBottom(watermark.getMarginBottom());
      }
      if (watermark.getMarginRight() != null) {
        builder.marginRight(watermark.getMarginRight());
      }
      if (watermark.getQuality() != null) {
        builder.quality(watermark.getQuality());
      }
      if (watermark.getSource() != null) {
        builder.source(resource);
      }
      if (watermark.getTransparency() != null) {
        builder.transparency(watermark.getTransparency());
      }
      return new Watermark(builder.build());
    }
  }

}
