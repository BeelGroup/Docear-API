package org.sciplore.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "children", propOrder = {
    "annotation"
})
public class XmlChildren {

	protected List<XmlAnnotation> annotation;
}
