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

import com.github.zhanhb.ckfinder.connector.api.Configuration;
import com.github.zhanhb.ckfinder.connector.api.ThumbnailProperties;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.core.io.InputStreamSource;

/**
 * Utils to operate on images.
 */
@Slf4j
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class ImageUtils {

  /**
   * allowed image extensions.
   */
  private static final List<String> ALLOWED_EXT = Arrays.asList("gif", "jpeg", "jpg", "png", "bmp", "xbm");

  /**
   * Resizes the image and writes it to the disk.
   *
   * @param sourceImage original image file.
   * @param width requested width
   * @param height requested height
   * @param quality requested destination file quality
   * @param destFile file to write to
   * @throws IOException when error occurs.
   */
  private static void resizeImage(BufferedImage sourceImage, int width,
          int height, float quality, Path destFile) throws IOException {
    String format = FileUtils.getExtension(destFile.getFileName().toString());
    format = format != null ? format.toLowerCase() : null;
    try (OutputStream out = Files.newOutputStream(destFile)) {
      try {
        Thumbnails.of(sourceImage).size(width, height).keepAspectRatio(false).outputQuality(quality).outputFormat(format).toOutputStream(out);
        // for some special files outputQuality couses error:
        //IllegalStateException inner Thumbnailator jar. When exception is thrown
        // image is resized without quality
        // When http://code.google.com/p/thumbnailator/issues/detail?id=9
        // will be fixed this try catch can be deleted. Only:
        //Thumbnails.of(sourceImage).size(width, height).keepAspectRatio(false)
        //  .outputQuality(quality).toOutputStream(out);
        // should remain.
      } catch (IllegalStateException e) {
        Thumbnails.of(sourceImage).size(width, height).keepAspectRatio(false).outputFormat(format).toOutputStream(out);
      }
    }
  }

  /**
   * create thumb file.
   *
   * @param orginFile origin image file.
   * @param file file to save thumb
   * @param thumbnail connector thumbnail properties
   * @return
   * @throws IOException when error occurs.
   */
  public static boolean createThumb(Path orginFile, Path file, ThumbnailProperties thumbnail)
          throws IOException {
    log.debug("createThumb");
    BufferedImage image;
    try (InputStream is = Files.newInputStream(orginFile)) {
      image = ImageIO.read(is);
    }
    if (image != null) {
      Dimension dimension = createThumbDimension(image,
              thumbnail.getMaxWidth(), thumbnail.getMaxHeight());
      FileUtils.createPath(file, true);
      if (image.getHeight() <= dimension.height
              && image.getWidth() <= dimension.width) {
        writeUntouchedImage(orginFile, file);
      } else {
        resizeImage(image, dimension.width, dimension.height,
                thumbnail.getQuality(), file);
      }
      return true;
    } else {
      log.error("Wrong image file");
    }
    return false;
  }

  /**
   * Uploads image and if the image size is larger than maximum allowed it
   * resizes the image.
   *
   * @param part servlet part
   * @param file file name
   * @param fileName name of file
   * @param conf connector configuration
   * @throws IOException when error occurs.
   */
  public static void createTmpThumb(InputStreamSource part, Path file, String fileName,
          Configuration conf) throws IOException {
    BufferedImage image;
    try (InputStream stream = part.getInputStream()) {
      image = ImageIO.read(stream);
      if (image == null) {
        throw new IOException("Wrong file");
      }
    }
    Dimension dimension = createThumbDimension(image, conf.getImgWidth(),
            conf.getImgHeight());
    if (dimension.width == 0 || dimension.height == 0
            || (image.getHeight() <= dimension.height && image.getWidth() <= dimension.width)) {
      try (InputStream stream = part.getInputStream()) {
        Files.copy(stream, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } else {
      resizeImage(image, dimension.width, dimension.height, conf.getImgQuality(), file);
    }
    if (log.isTraceEnabled()) {
      log.trace("thumb size: {}", Files.size(file));
    }
  }

  /**
   * Creates image file with fixed width and height.
   *
   * @param sourceFile input file
   * @param destFile file to save
   * @param width image width
   * @param height image height
   * @param quality image quality
   * @throws IOException when error occurs.
   */
  public static void createResizedImage(Path sourceFile,
          Path destFile, int width, int height,
          float quality) throws IOException {
    BufferedImage image;
    try (InputStream is = Files.newInputStream(sourceFile)) {
      image = ImageIO.read(is);
    }
    if (image.getHeight() <= height && image.getWidth() <= width) {
      writeUntouchedImage(sourceFile, destFile);
    } else {
      resizeImage(image, width, height, quality, destFile);
    }
  }

  /**
   * creates dimension of thumb.
   *
   * @param image original image.
   * @param maxWidth max thumb width
   * @param maxHeight max thumb height
   * @return dimension of thumb image.
   */
  private static Dimension createThumbDimension(BufferedImage image,
          int maxWidth, int maxHeight) {
    log.debug("image(w={},h={}), max w={}, max h={}", image.getWidth(), image.getHeight(), maxWidth, maxHeight);
    int width = image.getWidth(), height = image.getHeight();

    long w = (long) width * maxHeight;
    long h = (long) height * maxWidth;

    if (w > h) {
      if (width > maxWidth) {
        height = (int) Math.round(1.0 * h / width);
        width = maxWidth;
      }
    } else if (height > maxHeight) {
      width = (int) Math.round(1.0 * w / height);
      height = maxHeight;
    }
    log.debug("didmension w={}, h={}", width, height);
    return new Dimension(width, height);
  }

  /**
   * checks if file is image.
   *
   * @param file file to check
   * @return true if file is image.
   */
  public static boolean isImageExtension(Path file) {
    if (file != null) {
      String fileExt = FileUtils.getExtension(file.getFileName().toString());
      return fileExt != null && ALLOWED_EXT.contains(fileExt.toLowerCase());
    } else {
      return false;
    }
  }

  /**
   * check if image size isn't bigger then biggest allowed.
   *
   * @param part servlet part
   * @param conf connector configuration.
   * @return true if image size isn't bigger then biggest allowed.
   * @throws IOException when error occurs during reading image.
   */
  public static boolean checkImageSize(InputStreamSource part, Configuration conf)
          throws IOException {
    final int maxWidth = conf.getImgWidth();
    final int maxHeight = conf.getImgHeight();
    if (maxHeight == 0 && maxWidth == 0) {
      return true;
    }
    BufferedImage bi;
    try (InputStream stream = part.getInputStream()) {
      bi = ImageIO.read(stream);
    }
    if (bi != null) {
      log.debug("image size: {} {}", bi.getWidth(), bi.getHeight());
      return bi.getHeight() <= maxHeight && bi.getWidth() <= maxWidth;
    }
    return false;
  }

  /**
   * checks if image file is image.
   *
   * @param item file upload item
   * @return true if file is image.
   */
  public static boolean isValid(InputStreamSource item) {
    BufferedImage bi;
    try (InputStream is = item.getInputStream()) {
      bi = ImageIO.read(is);
    } catch (IOException e) {
      return false;
    }
    return bi != null;
  }

  /**
   * writes unchanged file to disk.
   *
   * @param sourceFile - file to read from
   * @param destFile - file to write to
   * @throws IOException when error occurs.
   */
  private static void writeUntouchedImage(Path sourceFile, Path destFile)
          throws IOException {
    Files.copy(sourceFile, destFile, StandardCopyOption.REPLACE_EXISTING);
  }

}
