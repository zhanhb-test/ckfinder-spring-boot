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
public class Errors implements ConnectorElement {

  @Singular
  @XmlElementRef(type = DetailError.class)
  private List<DetailError> errors;

}
