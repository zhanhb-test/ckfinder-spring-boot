package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.api.CKFinderContext;
import com.github.zhanhb.ckfinder.connector.api.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.response.ConnectorElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolderElement;
import com.github.zhanhb.ckfinder.connector.handlers.response.ErrorCodeElement;
import com.github.zhanhb.ckfinder.connector.support.CommandContext;
import com.github.zhanhb.ckfinder.connector.support.XmlCreator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

/**
 * Base class to handle XML commands.
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
    ConnectorElement connectorElement = buildConnector(param, cmdContext);
    String result = XmlCreator.INSTANCE.toString(connectorElement);

    response.setContentType(MediaType.APPLICATION_XML_VALUE);
    response.setCharacterEncoding("utf-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      out.write(result);
    }
  }

  abstract ConnectorElement buildConnector(T param, CommandContext cmdContext)
          throws ConnectorException;


  /**
   * creates <code>CurrentFolderElement</code>.
   *
   * @param cmdContext command context
   * @param rootElement XML root node.
   */
  final void createCurrentFolderNode(CommandContext cmdContext, ConnectorElement.Builder rootElement) {
    if (cmdContext.getType() != null && cmdContext.getCurrentFolder() != null) {
      rootElement.currentFolder(CurrentFolderElement.builder()
              .path(cmdContext.getCurrentFolder())
              .url(cmdContext.getType().getUrl()
                      + cmdContext.getCurrentFolder())
              .acl(cmdContext.getAcl())
              .build());
    }
  }

}
