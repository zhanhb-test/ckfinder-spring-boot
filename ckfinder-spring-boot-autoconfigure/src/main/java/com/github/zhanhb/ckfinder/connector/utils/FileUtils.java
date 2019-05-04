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
package com.github.zhanhb.ckfinder.connector.utils;

import com.github.zhanhb.ckfinder.connector.api.AccessControl;
import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.Constants;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.download.URLEncoder;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utils for files.
 *
 */
@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "NestedAssignment"})
public class FileUtils {

  /**
   * Gets list of children folder or files for dir, according to searchDirectory
   * param.
   *
   * @param dir folder to search.
   * @param searchDirectory if true method return list of folders, otherwise
   * list of files.
   * @return list of files or subdirectories in selected directory
   * @throws IOException when IO Exception occurs.
   */
  public static List<Path> listChildren(Path dir, boolean searchDirectory)
          throws IOException {
    DirectoryStream.Filter<Path> filter = searchDirectory ? Files::isDirectory : Files::isRegularFile;
    try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, filter)) {
      return StreamSupport.stream(ds.spliterator(), false).collect(Collectors.toList());
    }
  }

  /**
   * Gets file extension.
   *
   * @param fileName name of file.
   * @return file extension
   */
  @Nullable
  public static String getLongExtension(String fileName) {
    int indexOf;
    if (fileName == null || (indexOf = fileName.indexOf('.')) == -1
            || indexOf == fileName.length() - 1) {
      return null;
    }
    return fileName.substring(indexOf + 1);
  }

  /**
   * Gets file last extension.
   *
   * @param fileName name of file.
   * @return file extension
   */
  @Nullable
  public static String getExtension(String fileName) {
    int lastIndexOf;
    if (fileName == null || (lastIndexOf = fileName.lastIndexOf('.')) == -1
            || lastIndexOf == fileName.length() - 1) {
      return null;
    }
    return fileName.substring(lastIndexOf + 1);
  }

  /**
   * Gets file name without its extension.
   *
   * @param fileName name of file
   * @return file extension
   */
  @Nullable
  public static String getNameWithoutLongExtension(String fileName) {
    int indexOf;
    if (fileName == null || (indexOf = fileName.indexOf('.')) == -1) {
      return null;
    }
    return fileName.substring(0, indexOf);
  }

  /**
   * Gets file name without its last extension.
   *
   * @param fileName name of file
   * @return file extension
   */
  @Nullable
  public static String getNameWithoutExtension(String fileName) {
    int lastIndexOf;
    if (fileName == null || (lastIndexOf = fileName.lastIndexOf('.')) == -1) {
      return null;
    }
    return fileName.substring(0, lastIndexOf);
  }

  /**
   * Parse date with pattern yyyyMMddHHmm. Pattern is used in get file command
   * response XML.
   *
   * @param attributes input file attributes.
   * @return parsed file modification date.
   */
  public static String parseLastModifiedDate(BasicFileAttributes attributes) {
    Instant instant = attributes.lastModifiedTime().toInstant();
    return DateTimeFormatterHolder.FORMATTER.format(instant);
  }

  /**
   * deletes file or folder with all subfolders and subfiles.
   *
   * @param file file or directory to delete.
   * @return true if all files are deleted.
   */
  public static boolean delete(Path file) {
    try {
      DeleteHelper.delete(file);
      return true;
    } catch (IOException ex) {
      return false;
    }
  }

  /**
   * check if file or folder name doesn't match invalid name.
   *
   * @param fileName file name
   * @return true if file name is correct
   */
  public static boolean isFileNameValid(String fileName) {
    return !(fileName == null || fileName.isEmpty()
            || fileName.charAt(fileName.length() - 1) == '.'
            || fileName.contains("..")
            || hasInvalidCharacter(fileName));
  }

  /**
   * check if new folder name contains disallowed chars.
   *
   * @param fileName file name
   * @return true if it does contain disallowed characters.
   */
  private static boolean hasInvalidCharacter(@Nonnull String fileName) {
    return InvalidFileNamePatternHolder.INVALID_FILENAME_PATTERN.matcher(fileName).find();
  }

  /**
   * checks if file extension is on denied list or isn't on allowed list.
   *
   * @param fileName filename
   * @param type resource type
   * @return true if extension is allowed
   */
  public static boolean isFileExtensionAllowed(String fileName, ResourceType type) {
    if (type == null || fileName == null) {
      return false;
    }

    if (fileName.indexOf('.') == -1) {
      return true;
    }

    return isExtensionAllowed(getExtension(fileName), type);
  }

  /**
   * Checks whether files extension is allowed.
   *
   * @param fileExt a string representing file extension to test
   * @param type a {@code ResourceType} object holding list of allowed and
   * denied extensions against which parameter fileExt will be tested
   * @return {@code true} is extension is on allowed extensions list or if
   * allowed extensions is empty. The {@code false} is returned when file is on
   * denied extensions list or if none of the above conditions is met.
   */
  private static boolean isExtensionAllowed(String fileExt, ResourceType type) {
    StringTokenizer st = new StringTokenizer(type.getDeniedExtensions(), ",");
    while (st.hasMoreTokens()) {
      if (st.nextToken().equalsIgnoreCase(fileExt)) {
        return false;
      }
    }

    st = new StringTokenizer(type.getAllowedExtensions(), ",");
    //The allowedExtensions is empty. Allow everything that isn't dissallowed.
    if (!st.hasMoreTokens()) {
      return true;
    }

    do {
      if (st.nextToken().equalsIgnoreCase(fileExt)) {
        return true;
      }
    } while (st.hasMoreTokens());
    return false;
  }

  /**
   * converts filename to ASCII.
   *
   * @param fileName file name
   * @return encoded file name
   */
  public static String convertToAscii(String fileName) {
    return Utf8AccentsHolder.convert(fileName);
  }

  /**
   * creates file and all above folders that do not exist.
   *
   * @param file file to create.
   * @throws IOException when IO Exception occurs.
   */
  public static void createPath(Path file) throws IOException {
    Path dir = file.getParent();
    if (dir != null) {
      Files.createDirectories(dir);
    }
    Files.createFile(file);
  }

  /**
   * check if file size isn't bigger then max size for type.
   *
   * @param type resource type
   * @param fileSize file size
   * @return true if file size isn't bigger then max size for type.
   */
  public static boolean isFileSizeOutOfRange(ResourceType type, long fileSize) {
    final long maxSize = type.getMaxSize();
    return maxSize != 0 && maxSize <= fileSize;
  }

  /**
   * Checks if folder has any sub directory but respects ACL and hide directory
   * setting from context.
   *
   * @param accessControl access control the check the permission
   * @param dirPath path to current folder.
   * @param dir current folder being checked. Represented by File object.
   * @param context ckfinder context
   * @param resourceType name of resource type, folder is assigned to.
   * @param currentUserRole user role.
   * @return true if there are any allowed and non-hidden subfolders.
   */
  public static boolean hasChildren(AccessControl accessControl, String dirPath,
          Path dir, CKFinderContext context, String resourceType,
          String currentUserRole) {
    try (DirectoryStream<Path> list = Files.newDirectoryStream(dir, Files::isDirectory)) {
      for (Path path : list) {
        String subDirName = path.getFileName().toString();
        if (!context.isDirectoryHidden(subDirName)
                && accessControl.hasPermission(resourceType,
                        dirPath + subDirName, currentUserRole, AccessControl.FOLDER_VIEW)) {
          return true;
        }
      }
    } catch (IOException ignored) {
    }
    return false;
  }

  /**
   * rename file with double extension.
   *
   * @param type a {@code ResourceType} object holding list of allowed and
   * denied extensions against which file extension will be tested.
   * @param fileName file name
   * @return new file name with . replaced with _ (but not last)
   */
  public static String renameFileWithBadExt(ResourceType type, String fileName) {
    if (type == null || fileName == null) {
      return null;
    }

    if (fileName.indexOf('.') == -1) {
      return fileName;
    }

    StringTokenizer tokens = new StringTokenizer(fileName, ".");
    String currToken = tokens.nextToken();
    if (tokens.hasMoreTokens()) {
      StringBuilder cfileName = new StringBuilder(fileName.length()).append(currToken);
      boolean more;
      do {
        currToken = tokens.nextToken();
        more = tokens.hasMoreTokens();
        if (more) {
          cfileName.append(isExtensionAllowed(currToken, type) ? '.' : '_').append(currToken);
        } else {
          cfileName.append('.').append(currToken);
        }
      } while (more);
      return cfileName.toString();
    }
    return currToken;
  }

  public static String encodeURIComponent(String fileName) {
    return UriComponentHolder.URI_COMPONENT.encode(fileName);
  }

  public static boolean isFolderNameInvalid(String folderName, CKFinderContext context) {
    return (context.isDisallowUnsafeCharacters()
            && (folderName.contains(".") || folderName.contains(";")))
            || hasInvalidCharacter(folderName);
  }

  public static boolean isFileNameValid(String fileName, CKFinderContext context) {
    return (!context.isDisallowUnsafeCharacters() || !fileName.contains(";"))
            && isFileNameValid(fileName);
  }

  public static String escapeJavaScript(String fileName) {
    return fileName.replace("'", "\\'");
  }

  private interface InvalidFileNamePatternHolder {

    Pattern INVALID_FILENAME_PATTERN = Pattern.compile(Constants.INVALID_FILE_NAME_REGEX);

  }

  private interface UriComponentHolder {

    URLEncoder URI_COMPONENT = new URLEncoder("-_.*!'()~");

  }

  private interface DateTimeFormatterHolder {

    DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm", Locale.US)
            .withZone(ZoneId.systemDefault());

  }

}
