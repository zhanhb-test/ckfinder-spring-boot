package com.github.zhanhb.ckfinder.connector.handlers.parameter;

import com.github.zhanhb.ckfinder.connector.plugins.ImageResizeParam;
import java.util.EnumMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author zhanhb
 */
@Getter
@Setter
@SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
public class ImageResizeParameter extends Parameter {

  /**
   * file name
   */
  private String fileName;
  private String newFileName;
  private String overwrite;
  private Integer width;
  private Integer height;
  private boolean wrongReqSizesParams;
  private final Map<ImageResizeParam, String> sizesFromReq
          = new EnumMap<>(ImageResizeParam.class);

}
