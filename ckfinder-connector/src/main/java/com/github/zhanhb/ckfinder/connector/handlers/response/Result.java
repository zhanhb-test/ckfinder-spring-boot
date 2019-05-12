package com.github.zhanhb.ckfinder.connector.handlers.response;

import javax.xml.bind.annotation.XmlSeeAlso;

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
@SuppressWarnings("ClassMayBeInterface")
public abstract class Result implements ConnectorChild {
}
