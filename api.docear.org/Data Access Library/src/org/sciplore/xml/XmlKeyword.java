package org.sciplore.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.sciplore.resources.Keyword;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "keyword", propOrder = {
    "value"
})
public class XmlKeyword {
	
	@XmlValue
    protected String value;
    @XmlAttribute
    protected String keywordtype;
    
    public XmlKeyword(){}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKeywordtype() {
		return keywordtype;
	}

	public void setKeywordtype(String keywordtype) {
		this.keywordtype = keywordtype;
	}   
    
	public void setKeywordtype(int keywordtype) {
		switch(keywordtype){
			case Keyword.KEYWORD_TYPE_AUTHOR:
				this.setKeywordtype(ExternalizedStrings.getString("XmlKeyword.authorType")); //$NON-NLS-1$
				break;
			case Keyword.KEYWORD_TYPE_MINDMAP:
				this.setKeywordtype(ExternalizedStrings.getString("XmlKeyword.maindmapType")); //$NON-NLS-1$
				break;
			case Keyword.KEYWORD_TYPE_USER:
				this.setKeywordtype(ExternalizedStrings.getString("XmlKeyword.userType")); //$NON-NLS-1$
				break;
		}
	}   
}
