package com.github.zhanhb.ckfinder.connector.handlers.command;

import com.github.zhanhb.ckfinder.connector.configuration.Constants;
import com.github.zhanhb.ckfinder.connector.configuration.IConfiguration;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import com.github.zhanhb.ckfinder.connector.handlers.arguments.XMLArguments;
import com.github.zhanhb.ckfinder.connector.handlers.response.Connector;
import java.util.function.Supplier;

/**
 *
 * @author zhanhb
 * @param <T>
 */
@SuppressWarnings("FinalMethod")
public abstract class OnSuccessXmlCommand<T extends XMLArguments> extends XMLCommand<T> {

  public OnSuccessXmlCommand(Supplier<T> argumentsSupplier) {
    super(argumentsSupplier);
  }

  @Override
  protected final void createXMLChildNodes(int errorNum, Connector.Builder rootElement, T arguments, IConfiguration configuration) {
    if (errorNum == Constants.Errors.CKFINDER_CONNECTOR_ERROR_NONE) {
      createXMLChildNodesInternal(rootElement, arguments, configuration);
    }
  }

  protected abstract void createXMLChildNodesInternal(Connector.Builder rootElement, T arguments, IConfiguration configuration);

  @Override
  protected final int getDataForXml(T arguments, IConfiguration configuration) throws ConnectorException {
    createXml(arguments, configuration);
    return 0;
  }

  protected abstract void createXml(T arguments, IConfiguration configuration) throws ConnectorException;

}
