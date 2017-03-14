package com.github.zhanhb.ckfinder.connector.autoconfigure;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
@ConfigurationProperties(prefix = CKFinderProperties.CKFINDER_PREFIX)
@SuppressWarnings({"PublicInnerClass", "ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter"})
public class CKFinderProperties {

  public static final String CKFINDER_PREFIX = "ckfinder";

  private Boolean enabled;
  private String baseDir;
  private String baseUrl;
  private License license = new License();
  private Image image = new Image();
  private String[] defaultResourceTypes;
  private Map<String, Type> types;
  private String userRoleSessionVar;
  private AccessControl[] accessControls;
  private Thumbs thumbs = new Thumbs();
  private Boolean disallowUnsafeCharacters;
  private Boolean checkDoubleExtension;
  private Boolean checkSizeAfterScaling;
  private Boolean secureImageUploads;
  private String[] htmlExtensions;
  private Boolean forceAscii;
  private String[] hideFolders;
  private String[] hideFiles;
  private Watermark watermark = new Watermark();
  private ImageResize imageResize = new ImageResize();

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

    private Integer width;
    private Integer height;
    private Float quality;

  }

  @Getter
  @Setter
  public static class Type {

    private String url;
    private String directory;
    private Integer maxSize;
    private String[] allowedExtensions;
    private String[] deniedExtensions;

  }

  @Getter
  @Setter
  public static class Thumbs {

    private Boolean enabled;
    private String url;
    private String directory;
    private Boolean directAccess;
    private Integer maxHeight;
    private Integer maxWidth;
    private Float quality;

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

    private Boolean enabled;
    private Map<String, String> params;

  }

  @Getter
  @Setter
  public static class Watermark {

    private Boolean enabled;
    private String source;
    private Float transparency;
    private Float quality;
    private Integer marginBottom;
    private Integer marginRight;

  }

}
