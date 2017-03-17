package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.data.FilePostParam;
import com.github.zhanhb.ckfinder.connector.data.ResourceType;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author zhanhb
 */
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class RequestFileHelper {

  /**
   * get file list to copy from request.
   *
   * @param request request
   */
  static void addFilesListFromRequest(HttpServletRequest request, List<FilePostParam> files,
          IConfiguration configuration) {
    Map<String, ResourceType> types = configuration.getTypes();
    for (int i = 0;; ++i) {
      String paramName = "files[" + i + "][name]";
      String name = request.getParameter(paramName);
      if (name == null) {
        break;
      }
      String folder = request.getParameter("files[" + i + "][folder]");
      String options = request.getParameter("files[" + i + "][options]");
      String type = request.getParameter("files[" + i + "][type]");
      files.add(FilePostParam.builder().name(name).folder(folder).options(options).type(types.get(type)).build());
    }
  }

}
