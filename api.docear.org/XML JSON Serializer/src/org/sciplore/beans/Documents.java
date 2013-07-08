package org.sciplore.beans;

import org.sciplore.annotation.SciBeanElements;
import org.sciplore.formatter.CollectionBean;

@SciBeanElements({"document"})
public class Documents extends CollectionBean {
	
	private String href;
	private String totalAmount;

	public void setHref(String href) {
		this.href = href;
	}

	public String getHref() {
		return href;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}	
}
