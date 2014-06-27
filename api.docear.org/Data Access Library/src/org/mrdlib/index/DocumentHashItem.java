package org.mrdlib.index;

public class DocumentHashItem implements Comparable<DocumentHashItem> {
	public Integer documentId;
	public String pdfHash;
	public Integer rank;
	public Float relevance;
	public int documentsAvailable;
	
	@Override
	public int compareTo(DocumentHashItem o) {
		return this.rank.compareTo(o.rank);
	}
}