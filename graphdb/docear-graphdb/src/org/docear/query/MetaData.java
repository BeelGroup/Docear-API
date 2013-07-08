package org.docear.query;

import org.docear.xml.Variables;

public class MetaData {
	private Integer termCount = null;
	private Integer termsTotal = null;
	private Integer nodeCount = null;
	
	
	public Integer getTermCount() {
		return termCount;
	}

	public void setTermCount(Integer termCount) {
		this.termCount = termCount;
	}

	public Integer getTermsTotal() {
		return termsTotal;
	}

	public void setTermsTotal(Integer termsTotal) {
		this.termsTotal = termsTotal;
	}

	public Integer getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(Integer nodeCount) {
		this.nodeCount = nodeCount;
	}
	
	public Variables getVariables() {
		Variables variables = new Variables();
		if (termCount != null) {
			variables.addVariable("termCount", termCount.toString());
		}
		if (termsTotal != null) {
			variables.addVariable("termsTotal", termsTotal.toString());
		}
		if (nodeCount != null)  {
			variables.addVariable("nodeCount", nodeCount.toString());
		}
		
		return variables;
	}
}

