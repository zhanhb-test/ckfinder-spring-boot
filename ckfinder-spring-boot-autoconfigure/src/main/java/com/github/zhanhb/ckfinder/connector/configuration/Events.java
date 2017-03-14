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
package com.github.zhanhb.ckfinder.connector.configuration;

import com.github.zhanhb.ckfinder.connector.data.AfterFileUploadEventArgs;
import com.github.zhanhb.ckfinder.connector.data.AfterFileUploadEventHandler;
import com.github.zhanhb.ckfinder.connector.data.IEventHandler;
import com.github.zhanhb.ckfinder.connector.data.InitCommandEventArgs;
import com.github.zhanhb.ckfinder.connector.data.InitCommandEventHandler;
import com.github.zhanhb.ckfinder.connector.errors.ConnectorException;
import java.util.List;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides support for event handlers.
 */
@Builder(builderClassName = "Builder")
@Slf4j
@ToString
public class Events {

  @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch", "UseSpecificCatch"})
  private static <T> void run(List<? extends IEventHandler<T>> handlers,
          T args, IConfiguration configuration) throws ConnectorException {
    log.trace("{}", handlers);
    try {
      for (IEventHandler<T> eventHandler : handlers) {
        eventHandler.runEventHandler(args, configuration);
      }
    } catch (ConnectorException ex) {
      throw ex;
    } catch (Exception e) {
      throw new ConnectorException(e);
    }
  }

  @Singular
  private final List<AfterFileUploadEventHandler> afterFileUploadEventHandlers;
  @Singular
  private final List<InitCommandEventHandler> initCommandEventHandlers;

  public void runAfterFileUpload(AfterFileUploadEventArgs args, IConfiguration configuration)
          throws ConnectorException {
    run(afterFileUploadEventHandlers, args, configuration);
  }

  public void runInitCommand(InitCommandEventArgs args, IConfiguration configuration) {
    try {
      run(initCommandEventHandlers, args, configuration);
    } catch (ConnectorException ex) {
      // impossible
      throw new AssertionError(ex);
    }
  }

}
