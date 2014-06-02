package org.docear.graphdb;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.neo4j.graphdb.Node;

public class QuerySession {
	private Set<Node> nodesBeforeExpanded = new HashSet<Node>();
	private Set<Node> nodesExpanded = new HashSet<Node>();
	private Set<Node> maps = new HashSet<Node>();
	private SortedSet<Date> allNodeDates = new TreeSet<Date>();
	private SortedSet<Date> algNodeDates = new TreeSet<Date>();
	
	
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
		return getNoDaysSinceMax(this.allNodeDates.first());
	}
	
	public Integer getNoDaysSinceMaxAlg() {
		return getNoDaysSinceMax(this.algNodeDates.first());
	}	
	
	public void addNewAllDate(long date) {
		Date truncatedDate = DateUtils.truncate(new Date(date), Calendar.DATE);		
		this.allNodeDates.add(truncatedDate);
	}
	
	public void addNewAlgDate(long date) {
		Date truncatedDate = DateUtils.truncate(new Date(date), Calendar.DATE);		
		this.algNodeDates.add(truncatedDate);
	}
	
	
	private Integer getNoDaysSinceMax(Date minDate) {
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		
		from.setTime(minDate);
		to.setTime(new Date());
		
		return daysBetween(from, to);
	}
	
    private int daysBetween(Calendar startDate, Calendar endDate) {
        Calendar date = (Calendar) startDate.clone();
        int daysBetween = 0;
        while (date.before(endDate)) {
            date.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }
	
}
