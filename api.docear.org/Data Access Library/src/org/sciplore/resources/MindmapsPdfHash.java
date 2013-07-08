package org.sciplore.resources;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "mindmaps_pdfhash")
public class MindmapsPdfHash extends Resource {
	
	@Column(name = "mindmap_id")
	private Long mindmapId;
	
	@Column(name = "pdfhash")
	private String pdfHash;

	private Integer count;

	public Long getMindmapId() {
		return mindmapId;
	}

	public void setMindmapId(Long mindmapId) {
		this.mindmapId = mindmapId;
	}

	public String getPdfHash() {
		return pdfHash;
	}

	public void setPdfHash(String pdfHash) {
		this.pdfHash = pdfHash;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	@Override
	public Resource getPersistentIdentity() {
		if (getId() != null) {
			return (Resource) getSession().get(this.getClass(), getId());
		}
		//some of our maps link 2000+ pdf files --> we can't do a select for every tuple
		//--> one select in the corresponding web service class: if mindmap revision is already in the table, then discard all hashs 
		return null;
//		return MindmapsPdfHashQueries.getItem(getSession(), mindmapId, pdfHash);
	}	
	
}
