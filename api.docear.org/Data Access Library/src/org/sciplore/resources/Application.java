package org.sciplore.resources;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.sciplore.tools.Tools;

/**
 * Resource class for applications.
 *
 * @author Mario Lipinski &lt;<a href="mailto:lipinski@sciplore.org">lipinski@sciplore.org</a>&gt;
 * @see Resource
 */
@Entity
@Table(name = "applications")
public class Application extends Resource {
	public static final Short STATUS_STABLE = 1;
	public static final Short STATUS_RELEASE_CANDIDATE = 2;
	public static final Short STATUS_BETA = 3;
	public static final Short STATUS_ALPHA = 4;
	public static final Short STATUS_DEVELOPMENT = 5;
	
	
	private Integer applicationId;
	@Column(nullable = false)
	private String name;
	@Column(name="identifier", nullable = false)
	private String key;
	@Column(nullable = false)
	private String version;
	@Column(nullable = false)
	private Integer versionMajor = 0;
	@Column(nullable = false)
	private Integer versionMid = 0;
	@Column(nullable = false)
	private Integer versionMinor = 0;
	@Column(nullable = false)
	private Date releaseDate;	
	@Column(nullable = false)
	private Short valid;
	private Short versionStatus;
	private Integer versionStatusNumber;
	private Integer build;
	private String releaseNote;
	private Short priority;
	private Short active = 0;
	
	public Application(){}
	
	public Application(Session s){
		this.setSession(s);
	}
	
	public Resource getPersistentIdentity() {
		return getApplication(this);
	}
	
	/**
	 * Returns an Application object from the database for an Application object.
	 *
	 * @param a the application object 
	 * @return the application object from the database or null if not found
	 */
	public Application getApplication(Application a) {
		if (a.getId() != null) {
			return this.getApplication(a.getId());
		} 
		else if (a.getKey() != null && a.getVersion() != null){
			return this.getApplication(a.getKey(), a.getVersion());
		}
		else {
			return this.getApplication(a.getName(), a.getBuildNumber());
		}
	}

	/**
	 * Returns the application object with the given identifier.
	 *
	 * @param id the identifier
	 * @return the application object from the database or null if not found
	 */
	public Application getApplication(Integer id) {
		return (Application)this.getSession().get(Application.class, id);
	}
	
	/**
	 * Returns an application object matching key and version.
	 *
	 * @param key the key
	 * @param version the version
	 * @return the application object from the database or null if not found
	 */
	public  Application getApplication(String key, String version) {
		return (Application)this.getSession().createCriteria(Application.class)
			.add(Restrictions.eq("key", key))
			.add(Restrictions.eq("version", version))
			.setMaxResults(1)
			.uniqueResult();
	}
	
	private Application getApplication(String name, Integer buildNumber) {
		return (Application)this.getSession().createCriteria(Application.class)
				.add(Restrictions.eq("name", name))
				.add(Restrictions.eq("build", buildNumber))
				.setMaxResults(1)
				.uniqueResult();
	}
	
	public  Application getApplication(String name) {
		return (Application)this.getSession().createCriteria(Application.class)
			.add(Restrictions.eq("name", name))			
			.setMaxResults(1)
			.uniqueResult();
	}
	
	
	@SuppressWarnings("unchecked")
	public  List<Application> getApplicationByAppId(Integer appID) {		
		return (List<Application>)this.getSession().createCriteria(Application.class)
			.add(Restrictions.eq("applicationId", appID)).list();		
	}
	
	public  Application getApplicationByAppId(Integer appID, String version) {		
		return (Application)this.getSession().createCriteria(Application.class)
			.add(Restrictions.eq("applicationId", appID))
			.add(Restrictions.eq("version", version))
			.setMaxResults(1)
			.uniqueResult();
	}
	
	public  Application getApplicationByAppVersion(String name, String version) {		
		return (Application)this.getSession().createCriteria(Application.class)
			.add(Restrictions.eq("name", name))
			.add(Restrictions.eq("version", version))
			.setMaxResults(1)
			.uniqueResult();
	}
	
	/**
	 * Synchronizes an Application object with a record from the database. If the 
	 * object does not exist, it is added to the database.
	 * In any case related objects are synchronized as well.
	 * 
	 * @param app the Application
	 * @return the synchronized Application which is stored in the database
	 */
	public Application sync(Application app) {
		Application a = getApplication(app);
		if(a == null) {
			a = app;
		} else {
			if(Tools.empty(a.getId()) && !Tools.empty(app.getId())) {
				a.setId(app.getId());
			}
			if(Tools.empty(a.getKey()) && !Tools.empty(app.getKey())) {
				a.setKey(app.getKey());
			}
			if(Tools.empty(a.getName()) && !Tools.empty(app.getName())) {
				a.setName(app.getName());
			}
			if(Tools.empty(a.getReleaseDate()) && !Tools.empty(app.getReleaseDate())) {
				a.setReleaseDate(app.getReleaseDate());
			}
			if(Tools.empty(a.getValid()) && !Tools.empty(app.getValid())) {
				a.setValid(app.getValid());
			}
			if(Tools.empty(a.getVersion()) && !Tools.empty(app.getVersion())) {
				a.setVersion(app.getVersion());
			}
		}
		return a;
	}
	
	
	
	/**
	 * Returns the application key.
	 * 
	 * @return the application key
	 */
	public String getKey() {
		return key;
	}
	
	/**
	 * Returns the application's name.
	 * 
	 * @return the application's name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the release date.
	 * 
	 * @return the release date
	 */
	public Date getReleaseDate() {
		return releaseDate;
	}
	
	/**
	 * Returns information about the validity of the record.
	 * 
	 * @return information about the validity of the record
	 */
	public Short getValid() {
		return valid;
	}
	
	/**
	 * Returns the version.
	 * 
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}
	
	
	
	/**
	 * Sets the application key.
	 * 
	 * @param key the application key
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * Sets the application's name.
	 * 
	 * @param name the application's name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the release date.
	 * 
	 * @param releaseDate the release date
	 */
	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}
	
	/**
	 * Sets information about the validity of the record.
	 * 
	 * @param valid Information about the validity of the record.
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
	
	/**
	 * Sets the version.
	 * 
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getVersionMajor() {
		return versionMajor;
	}

	public void setVersionMajor(Integer versionMajor) {
		this.versionMajor = versionMajor;
	}

	public Integer getVersionMid() {
		return versionMid;
	}

	public void setVersionMid(Integer versionMid) {
		this.versionMid = versionMid;
	}

	public Integer getVersionMinor() {
		return versionMinor;
	}

	public void setVersionMinor(Integer versionMinor) {
		this.versionMinor = versionMinor;
	}

	public Integer getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Integer applicationId) {
		this.applicationId = applicationId;
	}

	public Short getVersionStatus() {
		return versionStatus;
	}
	
	public String getVersionStatusName() {
		if(versionStatus == null) {
			return null;
		}
		else if (versionStatus == STATUS_BETA) {
			return "beta";
		}
		else if (versionStatus == STATUS_ALPHA) {
			return "alpha";
		}
		else if (versionStatus == STATUS_RELEASE_CANDIDATE) {
			return "rc";
		}
		else if (versionStatus == STATUS_STABLE) {
			return "stable";
		}
		else if (versionStatus == STATUS_DEVELOPMENT) {
			return "devel";
		}
		return null;
	}

	public void setVersionStatus(Short versionStatus) {
		this.versionStatus = versionStatus;
	}
	
	public void setVersionStatus(String statusName) {	
		if(statusName == null) {
			this.versionStatus = null;
		}
		else if ("beta".equals(statusName.trim().toLowerCase())) {
			this.versionStatus = STATUS_BETA;
		}
		else if ("alpha".equals(statusName.trim().toLowerCase())) {
			this.versionStatus = STATUS_ALPHA;
		}
		else if ("rc".equals(statusName.trim().toLowerCase()) || "release candidate".equals(statusName.trim().toLowerCase())) {
			this.versionStatus = STATUS_RELEASE_CANDIDATE;
		}
		else if ("stable".equals(statusName.trim().toLowerCase())) {
			this.versionStatus = STATUS_STABLE;
		}
		else if ("devel".equals(statusName.trim().toLowerCase())) {
			this.versionStatus = STATUS_DEVELOPMENT;
		}
		else {
			this.versionStatus = null;
		}
		
	}

	
	public Integer getVersionStatusNumber() {
		return versionStatusNumber;
	}

	public void setVersionStatusNumber(Integer versionStatusNumber) {
		this.versionStatusNumber = versionStatusNumber;
	}

	public Integer getBuildNumber() {
		return build;
	}

	public void setBuildNumber(Integer buildNumber) {
		this.build = buildNumber;
	}

	public String getReleaseNote() {
		return releaseNote;
	}

	public void setReleaseNote(String releaseNote) {
		this.releaseNote = releaseNote;
	}

	public Short getPriority() {
		return priority;
	}

	public void setPriority(Short priority) {
		this.priority = priority;
	}

	public Short getActive() {
		return active;
	}

	public void setActive(Short active) {
		this.active = active;
	}
}
