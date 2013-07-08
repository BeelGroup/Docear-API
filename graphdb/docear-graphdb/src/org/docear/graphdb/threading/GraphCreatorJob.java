package org.docear.graphdb.threading;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

public class GraphCreatorJob {
	public static final String PROPERTY_REVISION_KEY = "revision";
	public static final String PROPERTY_REVISION_TIMESTAMP = "revision_timestamp";

	public static final String PROPERTY_USERID_KEY = "user_id";
	public static final String PROPERTY_FILE_KEY = "file";
	public static final String CONTEXT_STYLE_MAP = "style_map";
	
	public static final String PROPERTY_ALLOW_CONTENT_RESEARCH = "allow_content_research";
	public static final String PROPERTY_ALLOW_INFORMATION_RETRIEVAL = "allow_information_retrieval";
	public static final String PROPERTY_ALLOW_USAGE_RESEARCH = "allow_usage_research";
	public static final String PROPERTY_ALLOW_RECOMMENDATIONS = "allow_recommendations";
	public static final String PROPERTY_AFFILIATION = "affiliation";
	public static final String PROPERTY_MAP_TYP = "map_type";
	public static final String PROPERTY_APPLICATION_BUILD = "app_build";
		
	private List<IGraphStep> jobSteps = new Vector<IGraphStep>();
	private final String name;
	private final String revision;
	private final String revisionTimestamp;
	private final Integer userID;
	private final File file;
	
	private final Map<Object, Object> context = new HashMap<Object, Object>();
	
	private boolean aborted = false; 

	public GraphCreatorJob(String name, Properties properties) throws InstantiationException {
		this.name = name;
		context.putAll(properties);
		if(properties.containsKey(PROPERTY_REVISION_KEY)) {
			this.revision = properties.getProperty(PROPERTY_REVISION_KEY);
		}
		else {
			throw new InstantiationException("revision property is required");
		}
		
		if(properties.containsKey(PROPERTY_REVISION_TIMESTAMP)) {
			this.revisionTimestamp = properties.getProperty(PROPERTY_REVISION_TIMESTAMP);
		}
		else {
			throw new InstantiationException("timestamp property is required");
		}
		
		if(properties.containsKey(PROPERTY_USERID_KEY)) {
			this.userID = Integer.parseInt(properties.getProperty(PROPERTY_USERID_KEY));
		}
		else {
			throw new InstantiationException("user_id property is required");
		}
		
		if(properties.containsKey(PROPERTY_FILE_KEY)) {
			this.file = new File(properties.getProperty(PROPERTY_FILE_KEY));
		}
		else {
			throw new InstantiationException("file property is required");
		}
		
		if(!properties.containsKey(PROPERTY_ALLOW_CONTENT_RESEARCH)) {
			throw new InstantiationException(PROPERTY_ALLOW_CONTENT_RESEARCH+" property is required");
		}
		
		if(!properties.containsKey(PROPERTY_ALLOW_INFORMATION_RETRIEVAL)) {
			throw new InstantiationException(PROPERTY_ALLOW_INFORMATION_RETRIEVAL+" property is required");
		}
		
		if(!properties.containsKey(PROPERTY_ALLOW_RECOMMENDATIONS)) {
			throw new InstantiationException(PROPERTY_ALLOW_RECOMMENDATIONS+" property is required");
		}
		
		if(!properties.containsKey(PROPERTY_ALLOW_USAGE_RESEARCH)) {
			throw new InstantiationException(PROPERTY_ALLOW_USAGE_RESEARCH+" property is required");
		}
		
		if(!properties.containsKey(PROPERTY_APPLICATION_BUILD)) {
			throw new InstantiationException(PROPERTY_APPLICATION_BUILD+" property is required");
		}
	}

	public synchronized void appendStep(IGraphStep step) {
		if (step == null) {
			throw new IllegalArgumentException("null-argument is not allowed");
		}
		jobSteps.add(step);
	}
	
	public synchronized void prependStep(IGraphStep step) {
		if (step == null) {
			throw new IllegalArgumentException("null-argument is not allowed");
		}
		jobSteps.add(0, step);
	}

	public synchronized IGraphStep next() {
		if (jobSteps.size() <= 0) {
			throw new IndexOutOfBoundsException();
		}
		return jobSteps.remove(0);
	}

	public synchronized boolean hasNext() {
		return jobSteps.size() > 0;
	}

	public String getName() {
		return name;
	}
	
	public String getRevision() {
		return revision;
	}

	public int size() {
	    return jobSteps.size();
	}

	public Integer getUserID() {
		return userID;
	}

	public File getFile() {
		return file;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}
	
	public void setContext(Object key, Object value) {
		this.context.put(key, value);
	}
	
	public Object getFromContext(Object key) {
		return this.context.get(key);
	}

	public String getRevisionTimestamp() {
		return revisionTimestamp;
	}
	
}
