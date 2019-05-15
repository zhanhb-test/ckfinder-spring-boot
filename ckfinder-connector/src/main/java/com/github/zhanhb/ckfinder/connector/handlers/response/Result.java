package com.github.zhanhb.ckfinder.connector.handlers.response;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

@XmlSeeAlso({
  ConnectorInfoElement.class,
  CopyFilesElement.class,
  DeleteFilesElement.class,
  FilesElement.class,
  FoldersElement.class,
  ImageInfoElement.class,
  MoveFilesElement.class,
  NewFolderElement.class,
  PluginInfosElement.class,
  RenamedFileElement.class,
  RenamedFolderElement.class,
  ResourceTypesElement.class
})
@XmlTransient
@SuppressWarnings("ClassMayBeInterface")
public abstract class Result implements ConnectorChild {
}
