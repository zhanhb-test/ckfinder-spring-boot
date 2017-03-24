package com.github.zhanhb.ckfinder.connector.autoconfigure;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
@ConfigurationProperties(CKFinderProperties.CKFINDER_PREFIX)
@SuppressWarnings({"PublicInnerClass", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class CKFinderProperties {

  public static final String CKFINDER_PREFIX = "ckfinder";

  private boolean enabled = true;
  private Connector connector = new Connector();
  private String basePath;
  private String baseUrl = "/userfiles";
  private License license = new License();
  private Image image = new Image();
  private String[] defaultResourceTypes = {};
  private Map<String, Type> types;
  private String userRoleSessionVar = "CKFinder_UserRole";
  private AccessControl[] accessControls;
  private Thumbs thumbs = new Thumbs();
  private boolean disallowUnsafeCharacters = false;
  private boolean checkDoubleExtension = true;
  private boolean checkSizeAfterScaling = true;
  private boolean secureImageUploads = true;
  private String[] htmlExtensions = {"html", "htm", "xml", "js"};
  private boolean forceAscii = false;
  private String[] hiddenFolders = {".*", "CVS"};
  private String[] hiddenFiles = {".*"};
  private Watermark watermark = new Watermark();
  private ImageResize imageResize = new ImageResize();
  private Servlet servlet = new Servlet();

  @Getter
  @Setter
  public static class Connector {

    private boolean enabled = true;

  }

  @Getter
  @Setter
  public static class License {

    private LicenseStrategy strategy = LicenseStrategy.none;
    private String name;
    private String key;

  }

  public static enum LicenseStrategy {
    none, host, auth;
  }

  @Getter
  @Setter
  public static class Image {

    private int width = 500;
    private int height = 400;
    private float quality = 0.8f;

  }

  @Getter
  @Setter
  public static class Type {

    private String url;
    private String directory;
    private int maxSize = 0;
    private String[] allowedExtensions = {};
    private String[] deniedExtensions = {};

  }

  @Getter
  @Setter
  public static class Thumbs {

    private boolean enabled = true;
    private String url = "%BASE_URL%_thumbs";
    private String directory = "%BASE_DIR%/_thumbs";
    private boolean directAccess = false;
    private int maxHeight = 100;
    private int maxWidth = 100;
    private float quality = 0.8f;

  }

  @Getter
  @Setter
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

  }

  @Getter
  @Setter
  public static class ImageResize {

    private boolean enabled = true;
    private Map<ImageResizeParamKey, String> params;

  }

  public static enum ImageResizeParamKey {
    smallThumb, mediumThumb, largeThumb
  }

  @Getter
  @Setter
  public static class Watermark {

    private boolean enabled = false;
    private String source;
    private Float transparency;
    private Float quality;
    private Integer marginBottom;
    private Integer marginRight;

  }

  @Getter
  @Setter
  public static class Servlet {

    private boolean enabled = true;
    private String[] path = {"/ckfinder/core/connector/java/connector.java"};

    public void setPath(String[] path) {
      Assert.notEmpty(path, "path should not be empty");
      Assert.notNull(path, "Path must not be null");
      for (String string : path) {
        Assert.isTrue(string.startsWith("/"), "Path must start with /");
      }
      this.path = path;
    }

  }

}
