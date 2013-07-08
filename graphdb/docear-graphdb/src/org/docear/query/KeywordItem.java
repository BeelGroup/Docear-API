package org.docear.query;


public class KeywordItem {
	private Double termWeighting;
	private String term;
	
	public KeywordItem(String term, Double termWeighting) {
		this.setTerm(term);
		this.setTermWeighting(termWeighting);
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public Double getTermWeighting() {
		return termWeighting;
	}

	public void setTermWeighting(Double termWeighting) {
		this.termWeighting = termWeighting;
	}
	
}