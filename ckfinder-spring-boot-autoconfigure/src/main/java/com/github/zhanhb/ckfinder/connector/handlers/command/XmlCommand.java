package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.Arguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import com.github.zhanhb.ckfinder.connector.utils.XMLCreator;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.function.Supplier;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author zhanhb
 * @param <T>
 */
public abstract class XmlCommand<T extends Arguments> extends Command<T> {

  protected XmlCommand(Supplier<T> supplier) {
    super(supplier);
  }

  /**
   * executes XML command. Creates XML response and writes it to response output
   * stream.
   *
   * @param arguments
   * @param configuration
   * @throws java.io.IOException
   */
  @Override
  @SuppressWarnings("FinalMethod")
  final void execute(T arguments, HttpServletRequest request, HttpServletResponse response, IConfiguration configuration)
          throws IOException, ConnectorException {
    Connector connector = buildConnector(arguments, configuration);

    response.setContentType("text/xml;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache");
    try (PrintWriter out = response.getWriter()) {
      XMLCreator.INSTANCE.writeTo(connector, out);
    }
  }

  abstract Connector buildConnector(T arguments, IConfiguration configuration)
          throws ConnectorException;

}
