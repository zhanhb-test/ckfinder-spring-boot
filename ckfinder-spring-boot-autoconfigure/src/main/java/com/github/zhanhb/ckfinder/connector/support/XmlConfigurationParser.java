package com.github.zhanhb.ckfinder.connector.support;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.BasePathBuilder;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ErrorCode;
import com.github.zhanhb.ckfinder.connector.api.ImageProperties;
import com.github.zhanhb.ckfinder.connector.api.License;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.api.ThumbnailProperties;
import com.github.zhanhb.ckfinder.connector.plugins.FileEditorPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeParam;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizePlugin;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeSize;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkSettings;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class loads configuration from XML file.
 *
 * @author zhanhb
 */
public enum XmlConfigurationParser {
  INSTANCE;

  /**
   * bytes in KB.
   */
  private static final int BYTES = 1024;

  private static final float MAX_QUALITY = 100f;

  /**
   *
   * @param resourceLoader resource loader to load xml configuration and
   * watermark resource
   * @param basePathBuilder base url and path builder
   * @param xmlFilePath string representation of the xml file
   * @return parsed configuration
   * @throws java.lang.Exception if exception occur when parse the resource
   */
  public DefaultCKFinderContext parse(ResourceLoader resourceLoader,
          BasePathBuilder basePathBuilder, String xmlFilePath)
          throws Exception {
    DefaultCKFinderContext.Builder builder = DefaultCKFinderContext.builder();
    Path basePath = basePathBuilder.getBasePath();
    init(builder, resourceLoader, xmlFilePath, basePath, basePathBuilder);
    return builder.build();
  }

  /**
   * Initializes configuration from XML file.
   *
   * @param builder context builder
   * @param basePathBuilder base url and path builder
   * @param resourceLoader resource loader to load xml configuration
   * @param basePath base path
   * @param xmlFilePath string representation of the xml file
   * @throws ConnectorException when error occurs
   * @throws IOException when IO Exception occurs.
   * @throws org.xml.sax.SAXException syntax error in xml file
   * @throws ParserConfigurationException no xml provider is avaliable
   */
  private void init(DefaultCKFinderContext.Builder builder, ResourceLoader resourceLoader,
          String xmlFilePath, Path basePath, BasePathBuilder basePathBuilder)
          throws ConnectorException, IOException, ParserConfigurationException, SAXException {
    Resource resource = getFullConfigPath(resourceLoader, xmlFilePath);
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc;
    try (InputStream stream = resource.getInputStream()) {
      doc = db.parse(stream);
    }
    doc.normalize();
    License.Builder licenseBuilder = License.builder().name("").key("");
    Node node = doc.getFirstChild();
    ThumbnailProperties thumbnail = null;
    ImageProperties.Builder image = ImageProperties.builder();
    if (node != null) {
      NodeList nodeList = node.getChildNodes();
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node childNode = nodeList.item(i);
        switch (childNode.getNodeName()) {
          case "enabled":
            builder.enabled(Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "licenseName":
            licenseBuilder.name(nullNodeToString(childNode));
            break;
          case "licenseKey":
            licenseBuilder.key(nullNodeToString(childNode));
            break;
          case "imgWidth":
            String width = nullNodeToString(childNode);
            width = width.replaceAll("\\D", "");
            try {
              image.maxWidth(Integer.parseInt(width));
            } catch (NumberFormatException e) {
              image.maxWidth(Constants.DEFAULT_IMG_WIDTH);
            }
            break;
          case "imgQuality":
            String quality = nullNodeToString(childNode);
            quality = quality.replaceAll("\\D", "");
            image.quality(adjustQuality(quality));
            break;
          case "imgHeight":
            String height = nullNodeToString(childNode);
            height = height.replaceAll("\\D", "");
            try {
              image.maxHeight(Integer.parseInt(height));
            } catch (NumberFormatException e) {
              image.maxHeight(Constants.DEFAULT_IMG_HEIGHT);
            }
            break;
          case "thumbs":
            thumbnail = createThumbs(childNode.getChildNodes(), basePath, basePathBuilder);
            break;
          case "accessControls":
            setACLs(builder, childNode.getChildNodes());
            break;
          case "hideFolders":
            setHiddenFolders(builder, childNode.getChildNodes());
            break;
          case "hideFiles":
            setHiddenFiles(builder, childNode.getChildNodes());
            break;
          case "checkDoubleExtension":
            builder.doubleFileExtensionsAllowed(!Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "disallowUnsafeCharacters":
            builder.disallowUnsafeCharacters(Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "forceASCII":
            builder.forceAscii(Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "checkSizeAfterScaling":
            builder.checkSizeAfterScaling(Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "secureImageUploads":
            builder.secureImageUploads(Boolean.parseBoolean(nullNodeToString(childNode)));
            break;
          case "uriEncoding":
            break;
          case "userRoleSessionVar":
            builder.userRoleName(nullNodeToString(childNode));
            break;
          case "defaultResourceTypes":
            String value = nullNodeToString(childNode);
            StringTokenizer sc = new StringTokenizer(value, ",");
            while (sc.hasMoreTokens()) {
              builder.defaultResourceType(sc.nextToken());
            }
            break;
          case "plugins":
            setPlugins(builder, childNode, resourceLoader);
            break;
          default:
            break;
        }
      }
    }
    builder.image(image.build());
    builder.licenseFactory(new FixLicenseFactory(licenseBuilder.build()));
    setTypes(builder.thumbnail(thumbnail), doc, basePathBuilder, thumbnail);
  }

  /**
   * Returns XML node contents or empty String instead of null if XML node is
   * empty.
   *
   * @param childNode the xml node
   * @return the text content
   */
  private String nullNodeToString(Node childNode) {
    String textContent = childNode.getTextContent();
    return textContent == null ? "" : textContent.trim();
  }

  /**
   * Gets absolute path to XML configuration file.
   *
   * @param resourceLoader resource loader to load xml configuration and
   * watermark resource
   * @param xmlFilePath string representation of the xml file
   * @return absolute path to XML configuration file
   * @throws ConnectorException when absolute path cannot be obtained.
   */
  private Resource getFullConfigPath(ResourceLoader resourceLoader, String xmlFilePath) throws ConnectorException {
    Resource resource = resourceLoader.getResource(xmlFilePath);
    if (!resource.exists()) {
      throw new ConnectorException(ErrorCode.FILE_NOT_FOUND,
              "Configuration file could not be found under specified location.");
    }
    return resource;
  }

  /**
   * Adjusts image quality.
   *
   * @param imgQuality Image quality
   * @return Adjusted image quality
   */
  private float adjustQuality(String imgQuality) {
    float helper;
    try {
      helper = Math.abs(Float.parseFloat(imgQuality));
    } catch (NumberFormatException e) {
      return Constants.DEFAULT_IMG_QUALITY;
    }
    if (helper == 0 || helper == 1) {
      return helper;
    } else if (helper > 0 && helper < 1) {
      helper = (Math.round(helper * MAX_QUALITY) / MAX_QUALITY);
    } else if (helper > 1 && helper <= MAX_QUALITY) {
      helper = (Math.round(helper) / MAX_QUALITY);
    } else {
      helper = Constants.DEFAULT_IMG_QUALITY;
    }
    return helper;
  }

  /**
   * Sets hidden files list defined in XML configuration.
   *
   * @param builder context builder
   * @param childNodes list of files nodes.
   */
  private void setHiddenFiles(DefaultCKFinderContext.Builder builder, NodeList childNodes) {
    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node node = childNodes.item(i);
      if (node.getNodeName().equals("file")) {
        String val = nullNodeToString(node);
        if (!val.isEmpty()) {
          builder.hiddenFile(val.trim());
        }
      }
    }
  }

  /**
   * Sets hidden folders list defined in XML configuration.
   *
   * @param builder context builder
   * @param childNodes list of folder nodes.
   */
  private void setHiddenFolders(DefaultCKFinderContext.Builder builder, NodeList childNodes) {
    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node node = childNodes.item(i);
      if (node.getNodeName().equals("folder")) {
        String val = nullNodeToString(node);
        if (!val.isEmpty()) {
          builder.hiddenFolder(val.trim());
        }
      }
    }
  }

  /**
   * Sets ACL configuration as a list of access control levels.
   *
   * @param builder context builder
   * @param childNodes nodes with ACL configuration.
   */
  private void setACLs(DefaultCKFinderContext.Builder builder, NodeList childNodes) {
    InMemoryAccessController accessControl = new InMemoryAccessController();
    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node childNode = childNodes.item(i);
      if (childNode.getNodeName().equals("accessControl")) {
        AccessControlLevel acl = getACLFromNode(childNode);
        if (acl != null) {
          accessControl.addPermission(acl);
        }
      }
    }
    builder.accessControl(accessControl);
  }

  /**
   * Gets single ACL configuration from XML node.
   *
   * @param childNode XML accessControl node.
   * @return access control level object.
   */
  private AccessControlLevel getACLFromNode(Node childNode) {
    String role = null;
    String resourceType = null;
    String folder = null;
    int mask = 0;
    for (int i = 0, j = childNode.getChildNodes().getLength(); i < j; i++) {
      Node childChildNode = childNode.getChildNodes().item(i);
      String nodeName = childChildNode.getNodeName();
      int index = 0;
      boolean bool = false;
      switch (nodeName) {
        case "role":
          role = nullNodeToString(childChildNode);
          break;
        case "resourceType":
          resourceType = nullNodeToString(childChildNode);
          break;
        case "folder":
          folder = nullNodeToString(childChildNode);
          break;
        case "folderView":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FOLDER_VIEW;
          break;
        case "folderCreate":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FOLDER_CREATE;
          break;
        case "folderRename":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FOLDER_RENAME;
          break;
        case "folderDelete":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FOLDER_DELETE;
          break;
        case "fileView":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FILE_VIEW;
          break;
        case "fileUpload":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FILE_UPLOAD;
          break;
        case "fileRename":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FILE_RENAME;
          break;
        case "fileDelete":
          bool = Boolean.parseBoolean(nullNodeToString(childChildNode));
          index = AccessControl.FILE_DELETE;
          break;
      }
      if (index != 0) {
        if (bool) {
          mask |= index;
        } else {
          mask &= ~index;
        }
      }
    }

    if (resourceType == null || role == null) {
      return null;
    }

    if (folder == null || folder.isEmpty()) {
      folder = "/";
    }
    return AccessControlLevel.builder()
            .folder(folder)
            .resourceType(resourceType)
            .role(role)
            .mask(mask)
            .build();
  }

  /**
   * creates thumb properties from XML.
   *
   * @param childNodes list of thumb XML nodes
   * @param basePathBuilder base url and path builder
   * @param basePath base path
   * @throws ConnectorException when error occurs
   * @throws IOException when IO Exception occurs.
   */
  @SuppressWarnings("deprecation")
  private ThumbnailProperties createThumbs(NodeList childNodes, Path basePath, BasePathBuilder basePathBuilder) throws ConnectorException, IOException {
    boolean enabled = true;
    ThumbnailProperties.Builder thumbnail = ThumbnailProperties.builder();
    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node childNode = childNodes.item(i);
      switch (childNode.getNodeName()) {
        case "enabled":
          enabled = Boolean.parseBoolean(nullNodeToString(childNode));
          break;
        case "url":
          thumbnail.url(PathUtils.normalizeUrl(basePathBuilder.getBaseUrl() + nullNodeToString(childNode).replace(Constants.BASE_URL_PLACEHOLDER, "")));
          break;
        case "directory":
          String thumbsDir = nullNodeToString(childNode).replace(Constants.BASE_DIR_PLACEHOLDER, "");
          Path file = getPath(basePath, thumbsDir);
          if (file == null) {
            throw new ConnectorException(ErrorCode.FOLDER_NOT_FOUND,
                    "Thumbs directory could not be created using specified path.");
          }
          thumbnail.path(Files.createDirectories(file));
          break;
        case "directAccess":
          thumbnail.directAccess(Boolean.parseBoolean(nullNodeToString(childNode)));
          break;
        case "maxHeight":
          String width = nullNodeToString(childNode);
          width = width.replaceAll("\\D", "");
          try {
            thumbnail.maxHeight(Integer.valueOf(width));
          } catch (NumberFormatException e) {
            thumbnail.maxHeight(Constants.DEFAULT_THUMB_MAX_WIDTH);
          }
          break;
        case "maxWidth":
          width = nullNodeToString(childNode);
          width = width.replaceAll("\\D", "");
          try {
            thumbnail.maxWidth(Integer.valueOf(width));
          } catch (NumberFormatException e) {
            thumbnail.maxWidth(Constants.DEFAULT_THUMB_MAX_HEIGHT);
          }
          break;
        case "quality":
          String quality = nullNodeToString(childNode);
          quality = quality.replaceAll("\\D", "");
          thumbnail.quality(adjustQuality(quality));
      }
    }
    return enabled ? thumbnail.build() : null;
  }

  /**
   * Creates resource types configuration from XML configuration file (from XML
   * element 'types').
   *
   * @param builder context builder
   * @param doc XML document.
   * @param basePathBuilder base url and path builder
   * @throws IOException when IO Exception occurs.
   * @throws ConnectorException when error occurs
   */
  private void setTypes(DefaultCKFinderContext.Builder builder, Document doc, BasePathBuilder basePathBuilder,
          ThumbnailProperties thumbnail)
          throws IOException, ConnectorException {
    NodeList list = doc.getElementsByTagName("type");

    for (int i = 0, j = list.getLength(); i < j; i++) {
      Element element = (Element) list.item(i);
      String name = element.getAttribute("name");
      if (name != null && !name.isEmpty()) {
        ResourceType resourceType = createTypeFromXml(name, element.getChildNodes(), basePathBuilder, thumbnail);
        builder.type(name, resourceType);
      }
    }
  }

  /**
   * Creates single resource type configuration from XML configuration file
   * (from XML element 'type').
   *
   * @param typeName name of type.
   * @param childNodes type XML child nodes.
   * @param basePathBuilder base url and path builder
   * @return parsed resource type
   * @throws IOException when IO Exception occurs.
   * @throws ConnectorException when error occurs
   */
  @SuppressWarnings("deprecation")
  private ResourceType createTypeFromXml(String typeName,
          NodeList childNodes, BasePathBuilder basePathBuilder, ThumbnailProperties thumbnail)
          throws IOException, ConnectorException {
    ResourceType.Builder builder = ResourceType.builder().name(typeName);
    String path = typeName.toLowerCase();
    String url = typeName.toLowerCase();

    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node childNode = childNodes.item(i);
      switch (childNode.getNodeName()) {
        case "url":
          url = nullNodeToString(childNode);
          break;
        case "directory":
          path = nullNodeToString(childNode);
          break;
        case "maxSize":
          long maxSize = 0;
          try {
            parseMaxSize(nullNodeToString(childNode));
          } catch (NumberFormatException | IndexOutOfBoundsException ex) {
          }
          builder.maxSize(maxSize);
          break;
        case "allowedExtensions":
          builder.allowedExtensions(nullNodeToString(childNode));
          break;
        case "deniedExtensions":
          builder.deniedExtensions(nullNodeToString(childNode));
      }
    }
    url = basePathBuilder.getBaseUrl() + url.replace(Constants.BASE_URL_PLACEHOLDER, "");
    url = PathUtils.normalizeUrl(url);
    String tpath = path.replace(Constants.BASE_DIR_PLACEHOLDER, "");

    Path p = getPath(basePathBuilder.getBasePath(), path);
    if (!p.isAbsolute()) {
      throw new ConnectorException(ErrorCode.FOLDER_NOT_FOUND,
              "Resource directory could not be created using specified path.");
    }
    Optional<Path> thumbnailPath = Optional.ofNullable(thumbnail).map(ThumbnailProperties::getPath).map(f -> PathUtils.resolve(f, tpath));
    return builder
            .url(url)
            .path(Files.createDirectories(p))
            .thumbnailPath(thumbnailPath).build();
  }

  /**
   * parses max size value from config (ex. 16M to number of bytes).
   *
   * @param maxSize string representation of the max size
   * @return number of bytes in max size.
   */
  private long parseMaxSize(String maxSize) {
    char lastChar = Character.toLowerCase(maxSize.charAt(maxSize.length() - 1));
    int a = 1, off = 1;
    switch (lastChar) {
      case 'k':
        a = BYTES;
        break;
      case 'm':
        a = BYTES * BYTES;
        break;
      case 'g':
        a = BYTES * BYTES * BYTES;
        break;
      default:
        off = 0;
        break;
    }
    long value = Long.parseLong(maxSize.substring(0, maxSize.length() - off));
    return value * a;
  }

  /**
   * Sets plugins list from XML configuration file.
   *
   * @param builder context builder
   * @param childNode child of XML node 'plugins'.
   * @param resourceLoader resource loader to load xml configuration and
   * watermark resource
   */
  private void setPlugins(DefaultCKFinderContext.Builder builder, Node childNode, ResourceLoader resourceLoader) {
    NodeList nodeList = childNode.getChildNodes();
    int length = nodeList.getLength();
    List<Plugin> plugins = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      Node childChildNode = nodeList.item(i);
      if ("plugin".equals(childChildNode.getNodeName())) {
        PluginInfo pluginInfo = createPluginFromNode(childChildNode);
        String name = pluginInfo.getName();
        if (name != null) {
          Plugin plugin;
          switch (name) {
            case "imageresize":
              try {
                plugin = new ImageResizePlugin(pluginInfo.getParams().entrySet().stream()
                        .collect(Collectors.toMap(entry
                                -> ImageResizeParam.valueOf(entry.getKey()),
                                entry -> new ImageResizeSize(entry.getValue()))));
              } catch (IllegalArgumentException ex) {
                plugin = new ImageResizePlugin(ImageResizeParam.createDefaultParams());
              }
              break;
            case "watermark":
              WatermarkSettings watermarkSettings = parseWatermarkSettings(pluginInfo, resourceLoader);
              plugin = new WatermarkPlugin(watermarkSettings);
              break;
            case "fileeditor":
              plugin = new FileEditorPlugin();
              break;
            default:
              continue;
          }
          plugins.add(plugin);
        }
      }
    }
    builder.eventsFromPlugins(plugins);
  }

  private WatermarkSettings parseWatermarkSettings(PluginInfo pluginInfo, ResourceLoader resourceLoader) {
    WatermarkSettings.Builder settings = WatermarkSettings.builder();
    for (Map.Entry<String, String> entry : pluginInfo.getParams().entrySet()) {
      final String name = entry.getKey();
      final String value = entry.getValue();
      switch (name) {
        case "source":
          settings.source(resourceLoader.getResource(value));
          break;
        case "transparency":
          settings.transparency(Float.parseFloat(value));
          break;
        case "quality":
          final int parseInt = Integer.parseInt(value);
          final int name1 = parseInt % 101;
          final float name2 = name1 / 100f;
          settings.quality(name2);
          break;
        case "marginBottom":
          settings.marginBottom(Integer.parseInt(value));
          break;
        case "marginRight":
          settings.marginRight(Integer.parseInt(value));
          break;
      }
    }
    return settings.build();
  }

  /**
   * Creates plugin data from configuration file.
   *
   * @param element XML plugin node.
   * @return PluginInfo data
   */
  private PluginInfo createPluginFromNode(Node element) {
    PluginInfo.Builder builder = PluginInfo.builder();
    NodeList list = element.getChildNodes();
    for (int i = 0, l = list.getLength(); i < l; i++) {
      Node childElem = list.item(i);
      String nodeName = childElem.getNodeName();
      String textContent = nullNodeToString(childElem);
      switch (nodeName) {
        case "name":
          builder.name(textContent);
          break;
        case "params":
          NodeList paramLlist = childElem.getChildNodes();
          for (int j = 0, m = paramLlist.getLength(); j < m; j++) {
            Node node = paramLlist.item(j);
            if ("param".equals(node.getNodeName())) {
              NamedNodeMap map = node.getAttributes();
              String name = null;
              String value = null;
              for (int k = 0, o = map.getLength(); k < o; k++) {
                Node item = map.item(k);
                String nodeName1 = item.getNodeName();
                if ("name".equals(nodeName1)) {
                  name = nullNodeToString(item);
                } else if ("value".equals(nodeName1)) {
                  value = nullNodeToString(item);
                }
              }
              builder.param(name, value);
            }
          }
          break;
      }
    }
    return builder.build();
  }

  private Path getPath(Path first, String... more) {
    return first == null ? null : first.getFileSystem().getPath(first.toString(), more);
  }

}
