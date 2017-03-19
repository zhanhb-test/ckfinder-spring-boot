package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.ConnectorServlet;
import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.DefaultPathBuilder;
import com.github.zhanhb.ckfinder.connector.configuration.FixLicenseFactory;
import com.github.zhanhb.ckfinder.connector.configuration.IBasePathBuilder;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.configuration.License;
import com.github.zhanhb.ckfinder.connector.configuration.LicenseFactory;
import com.github.zhanhb.ckfinder.connector.configuration.Plugin;
import com.github.zhanhb.ckfinder.connector.data.AccessControlLevel;
import com.github.zhanhb.ckfinder.connector.data.PluginInfo;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.plugins.FileEditorPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizePlugin;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkSettings;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.InMemoryAccessController;
import com.github.zhanhb.ckfinder.connector.utils.KeyGenerator;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import static com.github.zhanhb.ckfinder.connector.configuration.Constants.DEFAULT_BASE_URL;

/**
 *
 * @author zhanhb
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(CKFinderProperties.class)
@SuppressWarnings("PublicInnerClass")
@ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class CKFinderAutoConfiguration {

  @Configuration
  @ConditionalOnMissingBean(IBasePathBuilder.class)
  public static class DefaultBasePathBuilderConfigurer {

    @Bean
    public DefaultPathBuilder defaultPathBuilder(ApplicationContext applicationContext, CKFinderProperties properties) {
      ServletContext servletContext = applicationContext.getBean(ServletContext.class);
      String baseUrl = properties.getBaseUrl();
      if (StringUtils.isEmpty(baseUrl)) {
        baseUrl = DEFAULT_BASE_URL;
      }
      baseUrl = PathUtils.addSlashToBegin(PathUtils.addSlashToEnd(baseUrl));
      String basePath = properties.getBasePath();
      if (StringUtils.isEmpty(basePath)) {
        basePath = baseUrl.replaceAll("^/+", "");
      }
      try {
        Path path = Paths.get(basePath).normalize();
        basePath = path.toString();
        if (!path.isAbsolute()) {
          basePath = servletContext.getRealPath(basePath);
          if (basePath == null) {
            basePath = path.toString();
          }
        }
      } catch (IllegalArgumentException ex) {
      }
      return DefaultPathBuilder.builder().basePath(basePath).baseUrl(servletContext.getContextPath() + PathUtils.addSlashToEnd(baseUrl)).build();
    }

  }

  @Configuration
  @ConditionalOnMissingBean(AccessControl.class)
  public static class DefaultAccessControlConfigurer {

    private static int calc(int old, boolean condition, int mask) {
      return condition ? old | mask : old & ~mask;
    }

    @Bean
    public InMemoryAccessController defaultAccessControl(CKFinderProperties properties) {
      InMemoryAccessController accessControl = new InMemoryAccessController();
      CKFinderProperties.AccessControl[] accessControls = properties.getAccessControls();
      if (accessControls != null) {
        for (CKFinderProperties.AccessControl ac : accessControls) {
          String role = ac.getRole();
          String resourceType = ac.getResourceType();
          String folder = ac.getFolder();
          int mask = 0;
          mask = calc(mask, ac.isFileDelete(), AccessControl.FILE_DELETE);
          mask = calc(mask, ac.isFileRename(), AccessControl.FILE_RENAME);
          mask = calc(mask, ac.isFileUpload(), AccessControl.FILE_UPLOAD);
          mask = calc(mask, ac.isFileView(), AccessControl.FILE_VIEW);
          mask = calc(mask, ac.isFolderCreate(), AccessControl.FOLDER_CREATE);
          mask = calc(mask, ac.isFolderDelete(), AccessControl.FOLDER_DELETE);
          mask = calc(mask, ac.isFolderRename(), AccessControl.FOLDER_RENAME);
          mask = calc(mask, ac.isFolderView(), AccessControl.FOLDER_VIEW);

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
  public static class DefaultConfigurationConfigurer {

    private static String toString(String[] array) {
      return Arrays.stream(array).collect(Collectors.joining(","));
    }

    @Bean
    public IConfiguration ckfinderConfiguration(CKFinderProperties properties,
            IBasePathBuilder basePathBuilder,
            AccessControl defaultAccessControl,
            ObjectProvider<Collection<Plugin>> pluginsProvider) throws IOException {
      Collection<Plugin> plugins = pluginsProvider.getIfAvailable();
      com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder = com.github.zhanhb.ckfinder.connector.configuration.Configuration.builder()
              .enabled(properties.getConnector().isEnabled())
              .licenseFactory(createLiceFactory(properties.getLicense()));
      CKFinderProperties.Image image = properties.getImage();
      builder.imgWidth(image.getWidth())
              .imgHeight(image.getHeight())
              .imgQuality(image.getQuality());
      if (properties.getDefaultResourceTypes() != null) {
        builder.defaultResourceTypes(Arrays.asList(properties.getDefaultResourceTypes()));
      }
      if (properties.getTypes() != null) {
        setTypes(builder, basePathBuilder, properties.getTypes());
      }
      if (properties.getUserRoleSessionVar() != null) {
        builder.userRoleName(properties.getUserRoleSessionVar());
      }
      builder.accessControl(defaultAccessControl);
      setThumbs(properties.getThumbs(), basePathBuilder, builder);
      builder.disallowUnsafeCharacters(properties.isDisallowUnsafeCharacters())
              .checkDoubleFileExtensions(properties.isCheckDoubleExtension())
              .checkSizeAfterScaling(properties.isCheckSizeAfterScaling())
              .secureImageUploads(properties.isSecureImageUploads());
      if (properties.getHtmlExtensions() != null) {
        builder.htmlExtensions(Arrays.asList(properties.getHtmlExtensions()));
      }
      builder.forceAscii(properties.isForceAscii());
      if (properties.getHiddenFolders() != null) {
        builder.hiddenFolders(Arrays.asList(properties.getHiddenFolders()));
      }
      if (properties.getHiddenFiles() != null) {
        builder.hiddenFiles(Arrays.asList(properties.getHiddenFiles()));
      }
      builder.eventsFromPlugins(plugins != null ? plugins : Collections.emptyList());
      return builder.build();
    }

    private void setTypes(com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder,
            IBasePathBuilder basePathBuilder, Map<String, CKFinderProperties.Type> types) throws IOException {
      String basePath = basePathBuilder.getBasePath();
      String baseUrl = basePathBuilder.getBaseUrl();
      for (Map.Entry<String, CKFinderProperties.Type> entry : types.entrySet()) {
        final String typeName = entry.getKey();
        CKFinderProperties.Type type = entry.getValue();
        Assert.hasText(typeName, "Resource type name should not be empty");
        ResourceType.Builder resourceTypeBuilder = ResourceType.builder()
                .name(typeName);

        if (type.getAllowedExtensions() != null) {
          resourceTypeBuilder.allowedExtensions(toString(type.getAllowedExtensions()));
        }
        if (type.getDeniedExtensions() != null) {
          resourceTypeBuilder.deniedExtensions(toString(type.getDeniedExtensions()));
        }
        resourceTypeBuilder.maxSize(type.getMaxSize());
        String path = StringUtils.hasLength(type.getDirectory()) ? type.getDirectory() : typeName.toLowerCase();
        String url = type.getUrl() != null ? type.getUrl() : "/" + typeName.toLowerCase();

        resourceTypeBuilder.path(Files.createDirectories(Paths.get(basePath, path.replace(Constants.BASE_DIR_PLACEHOLDER, ""))).toString());
        resourceTypeBuilder.url(PathUtils.normalizeUrl(baseUrl + url.replace(Constants.BASE_URL_PLACEHOLDER, "")));

        builder.type(typeName, resourceTypeBuilder.build());
      }
    }

    private void setThumbs(CKFinderProperties.Thumbs thumbs, IBasePathBuilder basePathBuilder,
            com.github.zhanhb.ckfinder.connector.configuration.Configuration.Builder builder) {
      if (thumbs != null) {
        String basePath = basePathBuilder.getBasePath();
        String baseUrl = basePathBuilder.getBaseUrl();
        builder.thumbsEnabled(thumbs.isEnabled())
                .thumbsPath(Paths.get(basePath, thumbs.getDirectory().replace(Constants.BASE_DIR_PLACEHOLDER, "")).toString())
                .thumbsDirectAccess(thumbs.isDirectAccess())
                .thumbsUrl(PathUtils.normalizeUrl(baseUrl + thumbs.getUrl().replace(Constants.BASE_URL_PLACEHOLDER, "")))
                .maxThumbHeight(thumbs.getMaxHeight())
                .maxThumbWidth(thumbs.getMaxWidth())
                .imgQuality(thumbs.getQuality());
      }
    }

    private LicenseFactory createLiceFactory(CKFinderProperties.License license) {
      License.Builder licenseBuilder = License.builder()
              .name(license.getName()).key(license.getKey());
      CKFinderProperties.LicenseStrategy strategy = license.getStrategy();
      if (null != strategy) {
        switch (strategy) {
          case host:
            return new HostLicenseFactory();
          case auth:
            if (StringUtils.hasLength(license.getName())) {
              licenseBuilder.key(KeyGenerator.INSTANCE.generateKey(license.getName(), false));
            }
            break;
        }
      }
      return new FixLicenseFactory(licenseBuilder.build());
    }

  }

  @Configuration
  @ConditionalOnMissingBean(FileEditorPlugin.class)
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".file-editor", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static class DefaultFileEditorConfigurer {

    @Bean
    public FileEditorPlugin fileEditorPlugin() {
      return new FileEditorPlugin();
    }

  }

  @Configuration
  @ConditionalOnMissingBean(ImageResizePlugin.class)
  @ConditionalOnProperty(prefix = DefaultImageResizeConfigurer.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
  public static class DefaultImageResizeConfigurer {

    static final String PREFIX = CKFinderProperties.CKFINDER_PREFIX + ".image-resize";

    @Bean
    public ImageResizePlugin imageResizePlugin(CKFinderProperties properties) {
      CKFinderProperties.ImageResize imageResize = properties.getImageResize();
      Map<? extends Enum<?>, String> params = imageResize.getParams();
      PluginInfo.Builder pluginInfoBuilder = PluginInfo.builder();
      if (params != null && !params.isEmpty()) {
        for (Map.Entry<? extends Enum<?>, String> entry : params.entrySet()) {
          String key = entry.getKey().name();
          String value = entry.getValue();
          Assert.hasText(value, "thumbs size should not be empty");
          Assert.isTrue(value.matches("\\d+x\\d+"), "thumbs size '" + value + "' not correct");
          pluginInfoBuilder.param(key, value);
        }
      } else {
        pluginInfoBuilder
                .param("smallThumb", "90x90")
                .param("mediumThumb", "120x120")
                .param("largeThumb", "180x180");
      }
      return new ImageResizePlugin(pluginInfoBuilder.build().getParams());
    }

  }

  @Configuration
  @ConditionalOnMissingBean(WatermarkPlugin.class)
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".watermark", name = "enabled", havingValue = "true", matchIfMissing = false)
  public static class DefaultWatermarkConfigurer {

    @Bean
    public WatermarkPlugin watermarkPlugin(CKFinderProperties properties, ResourceLoader resourceLoader) {
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
      return new WatermarkPlugin(builder.build());
    }
  }

  @Configuration
  @ConditionalOnMissingBean(name = "connectorServlet")
  @ConditionalOnProperty(prefix = CKFinderProperties.CKFINDER_PREFIX + ".servlet", name = "enabled", havingValue = "true", matchIfMissing = true)
  public static class DefaultConnectorServletConfigurer {

    @Bean
    public ServletRegistrationBean connectorServlet(CKFinderProperties properties,
            MultipartConfigElement multipartConfigElement,
            IConfiguration configuration) {
      ConnectorServlet servlet = new ConnectorServlet(configuration);
      ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(servlet, false, properties.getServlet().getPath());
      servletRegistrationBean.setMultipartConfig(multipartConfigElement);
      return servletRegistrationBean;
    }

  }

}
