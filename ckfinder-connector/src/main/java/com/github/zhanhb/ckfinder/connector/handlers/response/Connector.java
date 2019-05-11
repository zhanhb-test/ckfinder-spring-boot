package com.github.zhanhb.ckfinder.connector.handlers.response;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 *
 * @author zhanhb
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Connector")
public class Connector {

  @XmlAttribute(name = "resourceType")
  private String resourceType;

  @XmlElementRef
  private CurrentFolder currentFolder;

  @XmlElementRef
  private Error error;

  @XmlElementRef
  private Errors errors;

  @Singular
  @XmlElementRef
  private List<Result> results;

}
