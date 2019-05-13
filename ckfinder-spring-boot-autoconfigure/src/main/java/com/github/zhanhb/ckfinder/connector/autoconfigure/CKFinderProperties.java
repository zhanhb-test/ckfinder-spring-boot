package com.github.zhanhb.ckfinder.connector.autoconfigure;

import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeParam;
import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeSize;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DataSizeUnit;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

/**
 * @author zhanhb
 */
@ConfigurationProperties(CKFinderProperties.CKFINDER_PREFIX)
@SuppressWarnings({"PublicInnerClass", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class CKFinderProperties {

  static final String CKFINDER_PREFIX = "ckfinder";

  private boolean enabled = true;
  private Connector connector = new Connector();
  private Base base = new Base();
  private License license = new License();
  private Image image = new Image();
  private String[] defaultResourceTypes = {};
  private Map<String, Type> types;
  private String userRoleSessionVar = "CKFinder_UserRole";
  private AccessControl[] accessControls;
  private Thumbs thumbs = new Thumbs();
  private boolean disallowUnsafeCharacters = false;
  private boolean allowDoubleExtension = false;
  private boolean csrf = true;
  private boolean checkSizeAfterScaling = true;
  private boolean secureImageUploads = true;
  private boolean forceAscii = false;
  private String[] hiddenFolders = {".*", "CVS"};
  private String[] hiddenFiles = {".*"};
  private Watermark watermark = new Watermark();
  private ImageResize imageResize = new ImageResize();
  private Servlet servlet = new Servlet();

  public boolean isEnabled() {
    return this.enabled;
  }

  public Connector getConnector() {
    return this.connector;
  }

  public Base getBase() {
    return this.base;
  }

  public License getLicense() {
    return this.license;
  }

  public Image getImage() {
    return this.image;
  }

  public String[] getDefaultResourceTypes() {
    return this.defaultResourceTypes;
  }

  public Map<String, Type> getTypes() {
    return this.types;
  }

  public String getUserRoleSessionVar() {
    return this.userRoleSessionVar;
  }

  public AccessControl[] getAccessControls() {
    return this.accessControls;
  }

  public Thumbs getThumbs() {
    return this.thumbs;
  }

  public boolean isDisallowUnsafeCharacters() {
    return this.disallowUnsafeCharacters;
  }

  public boolean isAllowDoubleExtension() {
    return this.allowDoubleExtension;
  }

  public boolean isCsrf() {
    return this.csrf;
  }

  public boolean isCheckSizeAfterScaling() {
    return this.checkSizeAfterScaling;
  }

  public boolean isSecureImageUploads() {
    return this.secureImageUploads;
  }

  public boolean isForceAscii() {
    return this.forceAscii;
  }

  public String[] getHiddenFolders() {
    return this.hiddenFolders;
  }

  public String[] getHiddenFiles() {
    return this.hiddenFiles;
  }

  public Watermark getWatermark() {
    return this.watermark;
  }

  public ImageResize getImageResize() {
    return this.imageResize;
  }

  public Servlet getServlet() {
    return this.servlet;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setConnector(Connector connector) {
    this.connector = connector;
  }

  public void setBase(Base base) {
    this.base = base;
  }

  public void setLicense(License license) {
    this.license = license;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  public void setDefaultResourceTypes(String[] defaultResourceTypes) {
    this.defaultResourceTypes = defaultResourceTypes;
  }

  public void setTypes(Map<String, Type> types) {
    this.types = types;
  }

  public void setUserRoleSessionVar(String userRoleSessionVar) {
    this.userRoleSessionVar = userRoleSessionVar;
  }

  public void setAccessControls(AccessControl[] accessControls) {
    this.accessControls = accessControls;
  }

  public void setThumbs(Thumbs thumbs) {
    this.thumbs = thumbs;
  }

  public void setDisallowUnsafeCharacters(boolean disallowUnsafeCharacters) {
    this.disallowUnsafeCharacters = disallowUnsafeCharacters;
  }

  public void setAllowDoubleExtension(boolean allowDoubleExtension) {
    this.allowDoubleExtension = allowDoubleExtension;
  }

  public void setCsrf(boolean csrf) {
    this.csrf = csrf;
  }

  public void setCheckSizeAfterScaling(boolean checkSizeAfterScaling) {
    this.checkSizeAfterScaling = checkSizeAfterScaling;
  }

  public void setSecureImageUploads(boolean secureImageUploads) {
    this.secureImageUploads = secureImageUploads;
  }

  public void setForceAscii(boolean forceAscii) {
    this.forceAscii = forceAscii;
  }

  public void setHiddenFolders(String[] hiddenFolders) {
    this.hiddenFolders = hiddenFolders;
  }

  public void setHiddenFiles(String[] hiddenFiles) {
    this.hiddenFiles = hiddenFiles;
  }

  public void setWatermark(Watermark watermark) {
    this.watermark = watermark;
  }

  public void setImageResize(ImageResize imageResize) {
    this.imageResize = imageResize;
  }

  public void setServlet(Servlet servlet) {
    this.servlet = servlet;
  }

  public static class Base {

    private String path;
    private String url = "/userfiles";

    public String getPath() {
      return path;
    }

    public String getUrl() {
      return url;
    }

    public void setPath(String path) {
      this.path = path;
    }

    public void setUrl(String url) {
      this.url = url;
    }

  }

  public static class Connector {

    private boolean enabled = true;

    public boolean isEnabled() {
      return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
      this.enabled = enabled;
    }
  }

  public static class License {

    private LicenseStrategy strategy = LicenseStrategy.NONE;
    private String name;
    private String key;

    public LicenseStrategy getStrategy() {
      return this.strategy;
    }

    public String getName() {
      return this.name;
    }

    public String getKey() {
      return this.key;
    }

    public void setStrategy(final LicenseStrategy strategy) {
      this.strategy = strategy;
    }

    public void setName(final String name) {
      this.name = name;
    }

    public void setKey(final String key) {
      this.key = key;
    }
  }

  public static class Image {

    private int maxWidth = 500;
    private int maxHeight = 400;
    private float quality = 0.8F;

    public int getMaxWidth() {
      return this.maxWidth;
    }

    public int getMaxHeight() {
      return this.maxHeight;
    }

    public float getQuality() {
      return this.quality;
    }

    public void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    public void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
    }

    public void setQuality(float quality) {
      this.quality = quality;
    }
  }

  public static class Type {

    private String url;
    private String directory;
    @DataSizeUnit(DataUnit.BYTES)
    private DataSize maxSize = DataSize.ofBytes(0);
    private String[] allowedExtensions = {};
    private String[] deniedExtensions = {};

    public String getUrl() {
      return this.url;
    }

    public String getDirectory() {
      return this.directory;
    }

    public DataSize getMaxSize() {
      return this.maxSize;
    }

    public String[] getAllowedExtensions() {
      return this.allowedExtensions;
    }

    public String[] getDeniedExtensions() {
      return this.deniedExtensions;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setDirectory(String directory) {
      this.directory = directory;
    }

    public void setMaxSize(DataSize maxSize) {
      this.maxSize = maxSize;
    }

    public void setAllowedExtensions(String[] allowedExtensions) {
      this.allowedExtensions = allowedExtensions;
    }

    public void setDeniedExtensions(String[] deniedExtensions) {
      this.deniedExtensions = deniedExtensions;
    }
  }

  public static class Thumbs {

    private boolean enabled = true;
    private String url = Constants.DEFAULT_THUMBS_URL;
    private String directory = Constants.DEFAULT_THUMBS_DIR;
    private boolean directAccess = false;
    private int maxHeight = 100;
    private int maxWidth = 100;
    private float quality = 0.8F;

    public boolean isEnabled() {
      return this.enabled;
    }

    public String getUrl() {
      return this.url;
    }

    public String getDirectory() {
      return this.directory;
    }

    public boolean isDirectAccess() {
      return this.directAccess;
    }

    public int getMaxHeight() {
      return this.maxHeight;
    }

    public int getMaxWidth() {
      return this.maxWidth;
    }

    public float getQuality() {
      return this.quality;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public void setDirectory(String directory) {
      this.directory = directory;
    }

    public void setDirectAccess(boolean directAccess) {
      this.directAccess = directAccess;
    }

    public void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
    }

    public void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    public void setQuality(float quality) {
      this.quality = quality;
    }
  }

  public static class AccessControl {

    private String role;
    private String resourceType;
    private String folder;
    private boolean folderView;
    private boolean folderCreate;
    private boolean folderRename;
    private boolean folderDelete;
    private boolean fileView;
    private boolean fileUpload;
    private boolean fileRename;
    private boolean fileDelete;

    public String getRole() {
      return this.role;
    }

    public String getResourceType() {
      return this.resourceType;
    }

    public String getFolder() {
      return this.folder;
    }

    public boolean isFolderView() {
      return this.folderView;
    }

    public boolean isFolderCreate() {
      return this.folderCreate;
    }

    public boolean isFolderRename() {
      return this.folderRename;
    }

    public boolean isFolderDelete() {
      return this.folderDelete;
    }

    public boolean isFileView() {
      return this.fileView;
    }

    public boolean isFileUpload() {
      return this.fileUpload;
    }

    public boolean isFileRename() {
      return this.fileRename;
    }

    public boolean isFileDelete() {
      return this.fileDelete;
    }

    public void setRole(String role) {
      this.role = role;
    }

    public void setResourceType(String resourceType) {
      this.resourceType = resourceType;
    }

    public void setFolder(String folder) {
      this.folder = folder;
    }

    public void setFolderView(boolean folderView) {
      this.folderView = folderView;
    }

    public void setFolderCreate(boolean folderCreate) {
      this.folderCreate = folderCreate;
    }

    public void setFolderRename(boolean folderRename) {
      this.folderRename = folderRename;
    }

    public void setFolderDelete(boolean folderDelete) {
      this.folderDelete = folderDelete;
    }

    public void setFileView(boolean fileView) {
      this.fileView = fileView;
    }

    public void setFileUpload(boolean fileUpload) {
      this.fileUpload = fileUpload;
    }

    public void setFileRename(boolean fileRename) {
      this.fileRename = fileRename;
    }

    public void setFileDelete(boolean fileDelete) {
      this.fileDelete = fileDelete;
    }
  }

  public static class ImageResize {

    private boolean enabled = true;
    private Map<ImageResizeParam, ImageResizeSize> params;

    public boolean isEnabled() {
      return this.enabled;
    }

    public Map<ImageResizeParam, ImageResizeSize> getParams() {
      return this.params;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void setParams(Map<ImageResizeParam, ImageResizeSize> params) {
      this.params = params;
    }
  }

  public static class Watermark {

    private boolean enabled = false;
    private Resource source;
    private float transparency = 0.8F;
    private float quality = 1;
    private int marginBottom = 5;
    private int marginRight = 5;

    public boolean isEnabled() {
      return this.enabled;
    }

    public Resource getSource() {
      return this.source;
    }

    public float getTransparency() {
      return this.transparency;
    }

    public float getQuality() {
      return this.quality;
    }

    public int getMarginBottom() {
      return this.marginBottom;
    }

    public int getMarginRight() {
      return this.marginRight;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public void setSource(Resource source) {
      this.source = source;
    }

    public void setTransparency(float transparency) {
      this.transparency = transparency;
    }

    public void setQuality(float quality) {
      this.quality = quality;
    }

    public void setMarginBottom(int marginBottom) {
      this.marginBottom = marginBottom;
    }

    public void setMarginRight(int marginRight) {
      this.marginRight = marginRight;
    }
  }

  public static class Servlet {

    private boolean enabled = true;
    private String[] path = {"/ckfinder/core/connector/java/connector.java"};

    public void setPath(String[] path) {
      Assert.notNull(path, "Path must not be null");
      Assert.notEmpty(path, "path should not be empty");
      for (String string : path) {
        Assert.isTrue(string.startsWith("/"), "Path must start with /");
      }
      this.path = path.clone();
    }

    public boolean isEnabled() {
      return this.enabled;
    }

    public String[] getPath() {
      return this.path;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }
  }

  public enum LicenseStrategy {
    NONE, HOST, AUTH
  }

}
