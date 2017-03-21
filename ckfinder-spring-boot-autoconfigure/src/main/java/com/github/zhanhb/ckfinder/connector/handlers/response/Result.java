package com.github.zhanhb.ckfinder.connector.handlers.response;

import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({
  ConnectorInfo.class,
  CopyFiles.class,
  DeleteFiles.class,
  Files.class,
  Folders.class,
  ImageInfo.class,
  MoveFiles.class,
  NewFolder.class,
  PluginsInfos.class,
  RenamedFile.class,
  RenamedFolder.class,
  ResourceTypes.class
})
@SuppressWarnings("ClassMayBeInterface")
public abstract class Result implements ConnectorElement {
}
