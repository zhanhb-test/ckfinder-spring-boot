package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ResourceType;
import com.github.zhanhb.ckfinder.connector.support.FilePostParam;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author zhanhb
 */
interface RequestFileHelper {

  /**
   * get file list to copy from request.
   *
   * @param request request
   * @param files uploaded files
   * @param context ckfinder context
   */
  static void addFilesListFromRequest(HttpServletRequest request, List<FilePostParam> files,
          CKFinderContext context) {
    for (int i = 0;; ++i) {
      String paramName = "files[" + i + "][name]";
      String name = request.getParameter(paramName);
      if (name == null) {
        break;
      }
      String folder = request.getParameter("files[" + i + "][folder]");
      String options = request.getParameter("files[" + i + "][options]");
      String type = request.getParameter("files[" + i + "][type]");
      files.add(FilePostParam.builder().name(name).folder(folder)
              .options(options).type(context.getResource(type)).build());
    }
  }

}
