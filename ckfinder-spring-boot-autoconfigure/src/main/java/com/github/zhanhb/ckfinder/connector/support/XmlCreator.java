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
package com.github.zhanhb.ckfinder.connector.support;

import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Class to create XML document.
 */
public enum XmlCreator {
  INSTANCE;

  private final ConcurrentMap<Class<?>, JAXBContext> contexts = new ConcurrentHashMap<>(4);

  public void writeTo(Object obj, Writer writer) {
    try {
      contexts.computeIfAbsent(obj.getClass(), classToBeBound -> {
        try {
          return JAXBContext.newInstance(classToBeBound);
        } catch (JAXBException ex) {
          throw new IllegalStateException("fail to instance xml transformer", ex);
        }
      }).createMarshaller().marshal(obj, writer);
    } catch (JAXBException e) {
      throw new IllegalStateException("fail to instance xml transformer", e);
    }
  }

  public String toString(Object obj) {
    StringWriter sw = new StringWriter();
    writeTo(obj, sw);
    return sw.toString();
  }

}
