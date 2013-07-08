package org.sciplore.resources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.sciplore.eventhandler.Required;
import org.sciplore.queries.MindmapQueries;
import org.sciplore.tools.SciploreResponseCode;

/**
 * This class represents a mind map
 */
@Entity
@Table(name = "mindmaps")
public class Mindmap extends Resource {

	public static Mindmap sync(Mindmap m) {
		// TODO
		return m;
	}
	
	private Boolean allowBackup = null;
	private Boolean allowContentResearch = null;
	private Boolean allowInformationRetrieval = null;
	private Boolean allowUsageResearch = null;
	private Boolean allowRecommendations = null;
	
	@Column(nullable = true)
	private String map_type = null;
	
	@Column(nullable = true)
	private String affiliation = null;
	
	@Column(nullable = false)
	private String mindmapId;
	@Column(name="publishInGallery", nullable = false)
	private Boolean publishInGallery;
	@Column(nullable = false)
	private Date revision;
	private Date uploaded;
	@ManyToOne
	@JoinColumn(nullable = false)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private User user;
	
	@ManyToOne
	@JoinColumn(nullable = true)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Application application;
	
	private String mapversion;
	private Integer filesize;
	private String filename;
	private String filepath;
	private String storagePath;
	
	@Column(nullable = false)
	private Short parsed;

	public Mindmap() {

	}
	
	public Mindmap(Session s) {
		this.setSession(s);
	}


//	public SciploreResponseCode create(User user,
//											 boolean allowIR, 
//											 boolean backup, 
//											 String mindmapId, 
//											 String mindmapName,
//											 String mindmapFile) {
//		if (user == null)
//			return new SciploreResponseCode(SciploreResponseCode.UNAUTHORIZED,
//					"User cannot be null.");
//
//		this.setAllowIR(allowIR);
//		this.setBackup(backup);
//		this.setUser(user);
//		this.setRevision(new GregorianCalendar().getTime());
//		this.setMindmapId(mindmapId);
//		this.setPublishInGallery(false);
//		this.setFilename(mindmapName);
//		this.setParsed((short)0);
//		try {
//			this.setFilesize(Tools.getFileSize(mindmapFile));
//		} catch (IOException e) {
//			System.out.println(Tools.getStackTraceAsString(e));
//			return new SciploreResponseCode(SciploreResponseCode.INTERNAL_SERVER_ERROR, e.getStackTrace().toString());
//		}		
//		this.save();
//		
//		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
//		String filename = this.getMindmapId() + "_"
//				+ format.format(this.getRevision());
//		return new SciploreResponseCode(SciploreResponseCode.OK, filename);
//	}
	
	public SciploreResponseCode create(User user, 
			 Application app,
			 boolean allowBackup, 
			 boolean allowContentResearch, 
			 boolean allowInformationRetrieval, 
			 boolean allowUsageResearch, 
			 boolean allowRecommendations, 
			 Date revision,
			 String mindmapId,
			 String mindmapName,
			 String mapPath,
			 int mapSize,
			 String storePath) {
		if (user == null)
		return new SciploreResponseCode(SciploreResponseCode.UNAUTHORIZED,
		"User cannot be null.");
		
		this.setAllowBackup(allowBackup);
		this.setAllowContentResearch(allowContentResearch);
		this.setAllowInformationRetrieval(allowInformationRetrieval);
		this.setAllowUsageResearch(allowUsageResearch);
		this.setAllowRecommendations(allowRecommendations);
		
		this.setUser(user);
		this.setRevision(revision);
		this.setUploaded(new Date());
		this.setMindmapId(mindmapId);
		this.setPublishInGallery(false);
		this.setFilename(mindmapName);
		this.setFilepath(mapPath);
		this.setParsed((short)0);
		this.setFilesize(mapSize);
		this.setStoragePath(storePath);
		this.setApplication(app);
		
		this.save();
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
		String filename = this.getMindmapId() + "_"
		+ format.format(this.getRevision());
		return new SciploreResponseCode(SciploreResponseCode.OK, filename);
	}
	
//	public void create(User user,
//									    boolean allowIR, 
//									    boolean backup,
//									    Date revision,
//									    Application app,
//									    String mindmapId, 
//									    String mindmapName,
//									    String mindmapFile) {
//	if (user == null)
//	return;
//	
//	this.setAllowIR(allowIR);
//	this.setBackup(backup);
//	this.setUser(user);
//	this.setRevision(revision);
//	this.setMindmapId(mindmapId);
//	this.setPublishInGallery(false);
//	this.setFilename(mindmapName);
//	this.setApplication(app);
//	this.setParsed((short)0);
//	try {
//		this.setFilesize(Tools.getFileSize(mindmapFile));
//	} catch (IOException e) {
//		System.out.println(Tools.getStackTraceAsString(e));	
//	}		
//	this.save();	
//	}
	
	public Resource getPersistentIdentity() {
		if (this.getId() == null) {
			return MindmapQueries.getMindmap(this.getSession(), this.getMindmapId(), this.getRevision());
		}
		return MindmapQueries.getMindmap(this.getSession(), this.getId());
	}

	/**
	 * @return the mindmapId
	 */
	public String getMindmapId() {
		return mindmapId;
	}

	/**
	 * @return the publishInGallery
	 */
	public Boolean getPublishInGallery() {
		return publishInGallery;
	}

	/**
	 * @return the revision
	 */
	public Date getRevision() {
		return revision;
	}

	public Date getUploaded() {
		return uploaded;
	}

	public void setUploaded(Date uploaded) {
		this.uploaded = uploaded;
	}

	/**
	 * @return the user
	 */
	public User getUser() {
		return user;
	}

	/**
	 * @param mindmapId
	 *            the mindmapId to set
	 */
	public void setMindmapId(String mindmapId) {
		this.mindmapId = mindmapId;
	}

	/**
	 * @param publishInGallery
	 *            the publishInGallery to set
	 */
	public void setPublishInGallery(Boolean publishInGallery) {
		this.publishInGallery = publishInGallery;
	}

	/**
	 * @param revision
	 *            the revision to set
	 */
	public void setRevision(Date revision) {
		this.revision = revision;
	}

	/**
	 * @param user
	 *            the user to set
	 */
	public void setUser(User user) {
		this.user = user;
	}

	public String getMapversion() {
		return mapversion;
	}

	public void setMapversion(String mapversion) {
		this.mapversion = mapversion;
	}

	public Integer getFilesize() {
		return filesize;
	}

	public void setFilesize(Integer filesize) {
		this.filesize = filesize;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getStoragePath() {
		return storagePath;
	}

	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

	public Application getApplication() {
		return application;
	}

	public int store(File mindmap, User user) {
		int mmid = 123;
		return mmid;
	}

	public void setParsed(Short parsed) {
		this.parsed = parsed;
	}

	public Short getParsed() {
		return parsed;
	}

	public Boolean getAllowBackup() {
	    return allowBackup;
	}

	public void setAllowBackup(Boolean allowBackup) {
	    this.allowBackup = allowBackup;
	}

	public Boolean getAllowInformationRetrieval() {
	    return allowInformationRetrieval;
	}

	public void setAllowInformationRetrieval(Boolean allowInformationRetrieval) {
	    this.allowInformationRetrieval = allowInformationRetrieval;
	}

	public Boolean getAllowUsageResearch() {
	    return allowUsageResearch;
	}

	public void setAllowUsageResearch(Boolean allowUsageResearch) {
	    this.allowUsageResearch = allowUsageResearch;
	}

	public Boolean getAllowContentResearch() {
	    return allowContentResearch;
	}

	public void setAllowContentResearch(Boolean allowContentResearch) {
	    this.allowContentResearch = allowContentResearch;
	}

	public Boolean getAllowRecommendations() {
	    return allowRecommendations;
	}

	public void setAllowRecommendations(Boolean allowRecommendations) {
	    this.allowRecommendations = allowRecommendations;
	}
	
	public boolean getAllowParsing() {
	    return getAllowContentResearch() ||
		    getAllowInformationRetrieval() ||
		    getAllowRecommendations() || 
		    getAllowUsageResearch();
	}

	public String getMapType() {
		return map_type;
	}

	public void setMapType(String map_type) {
		this.map_type = map_type;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
}
