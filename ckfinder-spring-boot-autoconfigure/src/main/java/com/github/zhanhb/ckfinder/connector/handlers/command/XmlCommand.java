package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.configuration.ParameterFactory;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.parameter.Parameter;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.handlers.response.CurrentFolder;
import com.github.zhanhb.ckfinder.connector.handlers.response.Error;
import com.github.zhanhb.ckfinder.connector.utils.AccessControl;
import com.github.zhanhb.ckfinder.connector.utils.XmlCreator;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 * @param <T>
 */
@SuppressWarnings("FinalMethod")
public abstract class XmlCommand<T extends Parameter> extends Command<T> {

  protected XmlCommand(ParameterFactory<T> paramFactory) {
    super(paramFactory);
  }

  /**
   * executes XML command. Creates XML response and writes it to response output
   * stream.
   *
   * @param param
   * @param request
   * @param response
   * @param configuration
   * @throws java.io.IOException
   * @throws com.github.zhanhb.ckfinder.connector.errors.ConnectorException
   */
  @Override
  final void execute(T param, HttpServletRequest request, HttpServletResponse response,
          IConfiguration configuration) throws IOException, ConnectorException {
    Connector connector = buildConnector(param, configuration);
    String result = XmlCreator.INSTANCE.toString(connector);

    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      out.write(result);
    }
  }

  abstract Connector buildConnector(T param, IConfiguration configuration)
          throws ConnectorException;

  final void createErrorNode(Connector.Builder rootElement, int code) {
    rootElement.error(Error.builder().number(code).build());
  }

  /**
   * creates <code>CurrentFolder</code> element.
   *
   * @param param
   * @param rootElement XML root node.
   * @param accessControl
   */
  final void createCurrentFolderNode(T param, Connector.Builder rootElement, AccessControl accessControl) {
    if (param.getType() != null && param.getCurrentFolder() != null) {
      rootElement.currentFolder(CurrentFolder.builder()
              .path(param.getCurrentFolder())
              .url(param.getType().getUrl()
                      + param.getCurrentFolder())
              .acl(accessControl.getAcl(param.getType().getName(), param.getCurrentFolder(), param.getUserRole()))
              .build());
    }
  }

}
