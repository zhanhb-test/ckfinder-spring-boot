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
package com.github.zhanhb.ckfinder.connector.plugins;

import com.github.zhanhb.ckfinder.connector.data.FileUploadEvent;
import com.github.zhanhb.ckfinder.connector.data.FileUploadListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.springframework.core.io.InputStreamSource;

@RequiredArgsConstructor
@Slf4j
public class WatermarkProcessor implements FileUploadListener {

  private final WatermarkSettings settings;

  @Override
  public void onFileUploadComplete(FileUploadEvent event) {
    try {
      final Path originalFile = event.getFile();
      final WatermarkPosition position = new WatermarkPosition(settings.getMarginBottom(), settings.getMarginRight());
      BufferedImage watermark = getWatermakImage(settings);
      if (watermark != null) {
        Thumbnails.of(originalFile.toFile())
                .watermark(position, watermark, settings.getTransparency())
                .scale(1)
                .outputQuality(settings.getQuality())
                .toFiles(Rename.NO_CHANGE);
      }
    } catch (Exception ex) {
      // only log error if watermark is not created
      log.error("", ex);
    }
  }

  /**
   * Extracts image location from settings or uses default image if none is
   * provided.
   *
   * @param settings
   * @return
   * @throws IOException
   */
  private BufferedImage getWatermakImage(WatermarkSettings settings) throws IOException {
    final InputStreamSource source = settings.getSource();
    if (source == null) {
      return null;
    }
    try (InputStream is = source.getInputStream()) {
      return is == null ? null : ImageIO.read(is);
    }

  }

}
