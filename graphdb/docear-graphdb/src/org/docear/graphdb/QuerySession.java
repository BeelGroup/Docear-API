package org.docear.graphdb;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.docear.Logging.DocearLogger;
import org.neo4j.graphdb.Node;

public class QuerySession {
	private Set<Node> nodesBeforeExpanded = new HashSet<Node>();
	private Set<Node> nodesExpanded = new HashSet<Node>();
	private Set<Node> maps = new HashSet<Node>();
	private SortedSet<Date> allNodeDates = new TreeSet<Date>();
	private SortedSet<Date> algNodeDates = new TreeSet<Date>();
	private Date filterDate = null;
	
	
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
	
	public SortedSet<Date> getNodeDates(boolean allUserMaps) {
		if (allUserMaps) {
			return allNodeDates;
		}
		else {
			return algNodeDates;
		}
	}
	
	public SortedSet<Date> getAlgNodeDates() {
		return algNodeDates;
	}

	public void setAlgNodeDates(SortedSet<Date> algNodeDates) {
		this.algNodeDates = algNodeDates;
	}

	public Integer getNoDaysSinceChosen() {
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		
		Date d = getFilterDate();
		if (d == null) {
			return null;
		}
		
		from.setTime(getFilterDate());
		to.setTime(new Date());
		
		return daysBetween(from, to);
	}
	
	public Integer getNoDaysSinceMax(boolean allUserMaps) {
		if (allUserMaps) {
			return getNoDaysSinceMax(this.allNodeDates.first());
		}
		else {
			return getNoDaysSinceMax(this.algNodeDates.first());
		}
	}	

	public void clearNewSinceMaxDates(boolean allUserMaps) {
		if (allUserMaps) {
			allNodeDates = new TreeSet<Date>();
		}
		else {
			algNodeDates = new TreeSet<Date>();
		}
		
	}
	
	public void addNewDate(long date, boolean allUserMaps) {
		Date truncatedDate = DateUtils.truncate(new Date(date), Calendar.DATE);
		if (allUserMaps) {
			this.allNodeDates.add(truncatedDate);
		}
		else {
			this.algNodeDates.add(truncatedDate);
		}
	}	
	
	public Date getFilterDate() {
		if (algNodeDates == null || algNodeDates.size() == 0) {
			DocearLogger.info("NDSM: algNodeDates empty");
			return null;
		}
		
		if (filterDate == null) {	
			int randomIndex = new Random().nextInt(algNodeDates.size());
			filterDate = algNodeDates.toArray(new Date[] {})[randomIndex];
		}
		
		return filterDate;
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
