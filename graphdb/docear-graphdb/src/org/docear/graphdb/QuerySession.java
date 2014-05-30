package org.docear.graphdb;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Node;

public class QuerySession {
	private Set<Node> nodesBeforeExpanded = new HashSet<Node>();
	private Set<Node> nodesExpanded = new HashSet<Node>();
	private Set<Node> maps = new HashSet<Node>();
	private long minDateSinceMaxAll = new Date().getTime();
	private long minDateSinceMaxAlg = new Date().getTime();
	
	public void addToNodesBeforeExpanded(Node node) {
		this.nodesBeforeExpanded.add(node);
	}

	public void addToNodesExpanded(Node node) {
		this.nodesExpanded.add(node);
	}
	
	public void addToMaps(Node node) {
		this.maps.add(node);
	}
	
	public int getNodeAmountBeforeExpanded() {
		return this.nodesBeforeExpanded.size();
	}
	
	public int getNodeAmountExpanded() {
		return this.nodesExpanded.size();
	}
	
	public int getMapAmount() {
		return this.maps.size();
	}	
	
	public Integer getNoDaysSinceMaxAll() {
		return daysBetween(this.minDateSinceMaxAll, new Date().getTime());
	}
	
	public Integer getNoDaysSinceMaxAlg() {
		return daysBetween(this.minDateSinceMaxAlg, new Date().getTime());
	}
	
	public void addNewSinceMaxAllDate(long date) {
		if (date < this.minDateSinceMaxAll) {
			this.minDateSinceMaxAll = date;
		}
	}
	
	public void addNewSinceMaxAlgDate(long date) {
		if (date < this.minDateSinceMaxAlg) {
			this.minDateSinceMaxAlg = date;
		}
	}
	
	private int daysBetween(Long d1, Long d2){
        return (int) (((d2-d1) / (1000 * 60 * 60 * 24)));
}
	
}
