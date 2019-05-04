package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.XmlCreator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 * @param <T> parameter type
 */
@SuppressWarnings("FinalMethod")
public abstract class XmlCommand<T> extends BaseCommand<T> {

  /**
   * executes XML command. Creates XML response and writes it to response output
   * stream.
   *
   * @param param the parameter
   * @param request request
   * @param response response
   * @param context ckfinder context
   * @throws IOException when IO Exception occurs.
   * @throws ConnectorException when error occurs
   */
  @Override
  final void execute(T param, HttpServletRequest request, HttpServletResponse response,
          CKFinderContext context) throws IOException, ConnectorException {
    CommandContext cmdContext = populateCommandContext(request, context);
    Connector connector = buildConnector(param, cmdContext);
    String result = XmlCreator.INSTANCE.toString(connector);

    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      out.write(result);
    }
  }

  abstract Connector buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException;

  final void createErrorNode(Connector.Builder rootElement, int code) {
    rootElement.error(Error.builder().number(code).build());
  }

  /**
   * creates <code>CurrentFolder</code> element.
   *
   * @param param the parameter
   * @param rootElement XML root node.
   * @param accessControl
   */
  final void createCurrentFolderNode(CommandContext cmdContext, Connector.Builder rootElement) {
    if (cmdContext.getType() != null && cmdContext.getCurrentFolder() != null) {
      rootElement.currentFolder(CurrentFolder.builder()
              .path(cmdContext.getCurrentFolder())
              .url(cmdContext.getType().getUrl()
                      + cmdContext.getCurrentFolder())
              .acl(cmdContext.getAcl())
              .build());
    }
  }

}
