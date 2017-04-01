package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.data.AccessControlLevel;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorError;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.plugins.FileEditorPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeParam;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizePlugin;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeSize;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkPlugin;
import com.github.zhanhb.ckfinder.connector.plugins.WatermarkSettings;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.InMemoryAccessController;
import com.github.zhanhb.ckfinder.connector.utils.PathUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

import static com.github.zhanhb.ckfinder.connector.configuration.Constants.DEFAULT_IMG_HEIGHT;
import static com.github.zhanhb.ckfinder.connector.configuration.Constants.DEFAULT_IMG_QUALITY;
import static com.github.zhanhb.ckfinder.connector.configuration.Constants.DEFAULT_IMG_WIDTH;
import static com.github.zhanhb.ckfinder.connector.configuration.Constants.DEFAULT_THUMB_MAX_WIDTH;

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
   * @param resourceLoader
   * @param basePathBuilder
   * @param xmlFilePath
   * @return
   * @throws java.lang.Exception
   */
  public Configuration parse(ResourceLoader resourceLoader,
          IBasePathBuilder basePathBuilder, String xmlFilePath)
          throws Exception {
    Configuration.Builder builder = Configuration.builder();
    Path basePath = basePathBuilder.getBasePath();
    init(builder, resourceLoader, xmlFilePath, basePath, basePathBuilder);
    return builder.build();
  }

  /**
   * Initializes configuration from XML file.
   *
   * @param builder
   * @param basePathBuilder
   * @param resourceLoader
   * @param basePath
   * @param xmlFilePath
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   * @throws java.io.IOException
   * @throws org.xml.sax.SAXException
   * @throws javax.xml.parsers.ParserConfigurationException
   */
  private void init(Configuration.Builder builder, ResourceLoader resourceLoader,
          String xmlFilePath, Path basePath, IBasePathBuilder basePathBuilder)
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
              builder.imgWidth(Integer.parseInt(width));
            } catch (NumberFormatException e) {
              builder.imgWidth(DEFAULT_IMG_WIDTH);
            }
            break;
          case "imgQuality":
            String quality = nullNodeToString(childNode);
            quality = quality.replaceAll("\\D", "");
            builder.imgQuality(adjustQuality(quality));
            break;
          case "imgHeight":
            String height = nullNodeToString(childNode);
            height = height.replaceAll("\\D", "");
            try {
              builder.imgHeight(Integer.parseInt(height));
            } catch (NumberFormatException e) {
              builder.imgHeight(DEFAULT_IMG_HEIGHT);
            }
            break;
          case "thumbs":
            setThumbs(builder, childNode.getChildNodes(), basePath, basePathBuilder);
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
            builder.checkDoubleFileExtensions(Boolean.parseBoolean(nullNodeToString(childNode)));
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
          case "htmlExtensions":
            String htmlExt = nullNodeToString(childNode);
            StringTokenizer scanner = new StringTokenizer(htmlExt, ",");
            while (scanner.hasMoreTokens()) {
              String val = scanner.nextToken();
              if (val != null && !val.isEmpty()) {
                builder.htmlExtension(val.trim().toLowerCase());
              }
            }
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
    builder.licenseFactory(new FixLicenseFactory(licenseBuilder.build()));
    setTypes(builder, doc, basePathBuilder);
  }

  /**
   * Returns XML node contents or empty String instead of null if XML node is
   * empty.
   *
   * @param childNode
   * @return
   */
  private String nullNodeToString(Node childNode) {
    String textContent = childNode.getTextContent();
    return textContent == null ? "" : textContent.trim();
  }

  /**
   * Gets absolute path to XML configuration file.
   *
   * @param resourceLoader
   * @param xmlFilePath
   * @return absolute path to XML configuration file
   * @throws ConnectorException when absolute path cannot be obtained.
   */
  private Resource getFullConfigPath(ResourceLoader resourceLoader, String xmlFilePath) throws ConnectorException {
    Resource resource = resourceLoader.getResource(xmlFilePath);
    if (!resource.exists()) {
      throw new ConnectorException(ConnectorError.FILE_NOT_FOUND,
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
      return DEFAULT_IMG_QUALITY;
    }
    if (helper == 0 || helper == 1) {
      return helper;
    } else if (helper > 0 && helper < 1) {
      helper = (Math.round(helper * MAX_QUALITY) / MAX_QUALITY);
    } else if (helper > 1 && helper <= MAX_QUALITY) {
      helper = (Math.round(helper) / MAX_QUALITY);
    } else {
      helper = DEFAULT_IMG_QUALITY;
    }
    return helper;
  }

  /**
   * Sets hidden files list defined in XML configuration.
   *
   * @param builder
   * @param childNodes list of files nodes.
   */
  private void setHiddenFiles(Configuration.Builder builder, NodeList childNodes) {
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
   * @param builder
   * @param childNodes list of folder nodes.
   */
  private void setHiddenFolders(Configuration.Builder builder, NodeList childNodes) {
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
   * @param builder
   * @param childNodes nodes with ACL configuration.
   */
  private void setACLs(Configuration.Builder builder, NodeList childNodes) {
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
   * creates thumb configuration from XML.
   *
   * @param builder
   * @param childNodes list of thumb XML nodes
   * @param basePathBuilder
   * @param basePath
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   * @throws java.io.IOException
   */
  @SuppressWarnings("deprecation")
  private void setThumbs(Configuration.Builder builder, NodeList childNodes, Path basePath, IBasePathBuilder basePathBuilder) throws ConnectorException, IOException {
    for (int i = 0, j = childNodes.getLength(); i < j; i++) {
      Node childNode = childNodes.item(i);
      switch (childNode.getNodeName()) {
        case "enabled":
          builder.thumbsEnabled(Boolean.parseBoolean(nullNodeToString(childNode)));
          break;
        case "url":
          builder.thumbsUrl(PathUtils.normalizeUrl(basePathBuilder.getBaseUrl() + nullNodeToString(childNode).replace(Constants.BASE_URL_PLACEHOLDER, "")));
          break;
        case "directory":
          String thumbsDir = nullNodeToString(childNode);
          Path file = basePath.getFileSystem().getPath(basePath.toString(), thumbsDir.replace(Constants.BASE_DIR_PLACEHOLDER, ""));
          if (file == null) {
            throw new ConnectorException(ConnectorError.FOLDER_NOT_FOUND,
                    "Thumbs directory could not be created using specified path.");
          }
          builder.thumbsPath(Files.createDirectories(file));
          break;
        case "directAccess":
          builder.thumbsDirectAccess(Boolean.parseBoolean(nullNodeToString(childNode)));
          break;
        case "maxHeight":
          String width = nullNodeToString(childNode);
          width = width.replaceAll("\\D", "");
          try {
            builder.maxThumbHeight(Integer.valueOf(width));
          } catch (NumberFormatException e) {
            builder.maxThumbHeight(DEFAULT_THUMB_MAX_WIDTH);
          }
          break;
        case "maxWidth":
          width = nullNodeToString(childNode);
          width = width.replaceAll("\\D", "");
          try {
            builder.maxThumbWidth(Integer.valueOf(width));
          } catch (NumberFormatException e) {
            builder.maxThumbWidth(DEFAULT_IMG_WIDTH);
          }
          break;
        case "quality":
          String quality = nullNodeToString(childNode);
          quality = quality.replaceAll("\\D", "");
          builder.thumbsQuality(adjustQuality(quality));
      }
    }
  }

  /**
   * Creates resource types configuration from XML configuration file (from XML
   * element 'types').
   *
   * @param builder
   * @param doc XML document.
   * @param basePathBuilder
   * @throws java.io.IOException
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  private void setTypes(Configuration.Builder builder, Document doc, IBasePathBuilder basePathBuilder)
          throws IOException, ConnectorException {
    NodeList list = doc.getElementsByTagName("type");

    for (int i = 0, j = list.getLength(); i < j; i++) {
      Element element = (Element) list.item(i);
      String name = element.getAttribute("name");
      if (name != null && !name.isEmpty()) {
        ResourceType resourceType = createTypeFromXml(name, element.getChildNodes(), basePathBuilder);
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
   * @param basePathBuilder
   * @return resource type
   * @throws java.io.IOException
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @SuppressWarnings("deprecation")
  private ResourceType createTypeFromXml(String typeName,
          NodeList childNodes, IBasePathBuilder basePathBuilder) throws IOException, ConnectorException {
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
          } catch (NumberFormatException ex) {
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

    Path p = basePathBuilder.getBasePath().getFileSystem().getPath(basePathBuilder.getBasePath().toString(), path.replace(Constants.BASE_DIR_PLACEHOLDER, ""));
    if (!p.isAbsolute()) {
      throw new ConnectorException(ConnectorError.FOLDER_NOT_FOUND,
              "Resource directory could not be created using specified path.");
    }
    return builder.url(url).path(Files.createDirectories(p)).build();
  }

  /**
   * parses max size value from config (ex. 16M to number of bytes).
   *
   * @param maxSize
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
   * @param builder
   * @param childNode child of XML node 'plugins'.
   * @param resourceLoader
   */
  private void setPlugins(Configuration.Builder builder, Node childNode, ResourceLoader resourceLoader) {
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
              WatermarkSettings watermarkSettings = checkPluginInfo(pluginInfo, resourceLoader);
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

  private WatermarkSettings checkPluginInfo(PluginInfo pluginInfo, ResourceLoader resourceLoader) {
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

}
