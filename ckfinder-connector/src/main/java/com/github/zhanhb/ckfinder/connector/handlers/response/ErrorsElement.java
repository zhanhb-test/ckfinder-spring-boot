package com.github.zhanhb.ckfinder.connector.handlers.response;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Singular;

/**
 *
 * @see com.github.zhanhb.ckfinder.connector.support.XmlCreator
 * @author zhanhb
 */
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "Errors")
public class ErrorsElement implements ConnectorChild {

  @Singular
  @XmlElementRef(type = ErrorElement.class)
  private List<ErrorElement> errors;

}
