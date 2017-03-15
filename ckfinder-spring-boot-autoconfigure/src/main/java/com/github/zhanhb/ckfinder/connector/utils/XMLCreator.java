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
package com.github.zhanhb.ckfinder.connector.utils;

import java.io.Writer;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * Class to create XML document.
 */
public enum XMLCreator {
  INSTANCE;

  public void writeTo(Object obj, Writer writer) {
    try {
      JAXBContext.newInstance(obj.getClass()).createMarshaller().marshal(obj, writer);
    } catch (JAXBException e) {
      throw new IllegalStateException("fail to instance xml transformer", e);
    }
  }

}
