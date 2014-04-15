package org.sciplore.resources;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Session;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.criterion.Restrictions;
import org.sciplore.eventhandler.Required;
import org.sciplore.tools.SciploreResponseCode;
import org.sciplore.tools.Tools;

@Entity
@Table(name = "users")
public class User extends Resource {
	
	public final static short USER_TYPE_ADMIN = 1;
	public final static short USER_TYPE_NORMAL = 2;
	public final static short USER_TYPE_ANONYMOUS = 3;
	public final static short USER_TYPE_EXTERNAL = 4;
	public final static short USER_TYPE_ARXIV = 5;
	public final static short USER_VALID = 1;
	public final static String USER_DEFAULT_LANG = "en";
	
	public static String crypt(String password) {
		String pass = "";
		try {
			pass = Tools.convertToSaltedMD5Digest("spl", password);
		} catch(NoSuchAlgorithmException e) {
		}
		return pass;
	}
	
	public  User getUser(String username) {
    	return (User) this.getSession().createCriteria(User.class)
    			.add(Restrictions.like("username", username))
    			.setMaxResults(1)
    			.uniqueResult();
	}
	
	public  User getUser(Person person) {
    	return (User) this.getSession().createCriteria(User.class)
    			.add(Restrictions.eq("person", person))
    			.setMaxResults(1)
    			.uniqueResult();
	}
	
	public  User getUser(User u) {
		if(u.getId() != null) {
			return (User) getSession().load(User.class, u.getId());			
		} else {
			return getUser(u.getUsername());
		}
	}
	
	public  User getUser(Integer id) {
		return (User)this.getSession().get(User.class, id);
	}
	
	public  User getUserByEmail(String email) {
		Contact contact = Contact.getContact(getSession(), email);
		if(contact != null) {
			return getUser(contact.getPerson());
		}
		else{
			return null;
		}    	
	}
	
	public  User getUserByEmailOrUsername(String value) {		
		User user = this.getUser(value);
		if(user != null){
			return user;
		}
		else{
			return this.getUserByEmail(value);
		}
	}
		
	@OneToMany(mappedBy="user")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Alert> alterts;
	@OneToMany(mappedBy="user")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Feedback> feedbacks;
	@Column(nullable = false)
	private String lang;
	@OneToMany(mappedBy="user")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Mindmap> mindmaps;
	@OneToMany(mappedBy="user")
	@Cascade(CascadeType.SAVE_UPDATE)
	private Set<Newsletter> newsletters;
	
	private String password;
	@OneToOne
	@JoinColumn(name ="person_id", nullable=true)
	@Cascade(CascadeType.SAVE_UPDATE)
	@Required
	private Person person;
	@Column(nullable = false)
	private Date registrationdate;
	private String remark;
	
	@Column(nullable = false)
	private Short type;
	
	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private Short valid = 1;
	
	@Column(nullable = true)
	private String accessToken;
	private Short allowBackup = null;
	private Short allowContentResearch = null;
	private Short allowInformationRetrieval = null;
	private Short allowUsageResearch = null;
	private Short allowRecommendations= null;
	
	@Column(nullable = true)
	private String remote_address;
	
	public User() {
		
	}
	
	public User(Session s) {
		this.setSession(s);
	}
	
	public User(Session s, String username) {
		this.setSession(s);
		this.username = username;
	}
	
	
	/**
	 * Constructur
	 * @param username
	 * @param password
	 */
	public User(Session s, String username, String password){
		this.setSession(s);
		this.username = username;
		this.password = password;
	}
	
	public boolean checkCredentials(String password) {
		if((this.password == null || this.password.isEmpty()) && (password == null || password.isEmpty())){
			return true;
		}
		if(this.password.equalsIgnoreCase(crypt(password)) || this.password.equalsIgnoreCase(password)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a new user in the database
	 * @return
	 */
	public int createUserinDB() {
		// create user in database AND a new person (since every user is a person)
		int userID = 1234;
		return userID;
	}
	
	public  SciploreResponseCode createUser(String username, 
			  					   String password,
			  					   String retypedPassword,
								   String eMail,
								   String firstName,
								   String middleName,
								   String lastName,
								   int birthYear,
								   boolean generalNewsLetter,
								   boolean searchNewsLetter,
								   boolean splmmNewsLetter,
								   short userType,
								   Boolean male, 
								   String remoteAddress){
		if(username == null || username.isEmpty()) 
			return new SciploreResponseCode(SciploreResponseCode.USERNAME_INVALID, "The user name must not be empty.");
		if(this.getUser(username) != null)
			return new SciploreResponseCode(SciploreResponseCode.USERNAME_NOT_AVAILABLE, "This user name is already in use. Please choose another one.");
		
		switch(userType){
			case User.USER_TYPE_ANONYMOUS:
				return createAnonymousUser(username, remoteAddress);
			case User.USER_TYPE_EXTERNAL:
				return createExternalUser(username, userType, remoteAddress);
			case User.USER_TYPE_NORMAL:
				return createNormalUser(username, 
								        password,
								        retypedPassword,
								        eMail,
								        firstName,
								        middleName,
								        lastName,
								        birthYear,
								        remoteAddress,
								        generalNewsLetter,
								        searchNewsLetter,
								        splmmNewsLetter,
								        male);
			default:
				return new SciploreResponseCode(SciploreResponseCode.INVALID_USER_TYPE, "Invalid user type.");
		}
	}
	
	private SciploreResponseCode createNormalUser(String username, 
									     String password,
									     String retypedPassword,
									     String eMail,
									     String firstName,
									     String middleName,
									     String lastName,
									     int birthYear, 
									     String remoteAddress,
									     boolean generalNewsLetter,
									     boolean searchNewsLetter,
									     boolean splmmNewsLetter,
									     Boolean male){
		if (!Tools.isValidUsername(username)) 
			return new SciploreResponseCode(SciploreResponseCode.USERNAME_INVALID, "The selected user name is invalid. (allowed characters are: 0-9, a-Z, ., -, _)");			
		
		if (eMail == null || !Tools.isValidEmailAddress(eMail))
			return new SciploreResponseCode(SciploreResponseCode.EMAIL_INVALID,	"Your email appears to be invalid. Please correct it.");			
						
		if (password == null || !(password.length() >= 6)) 
			return new SciploreResponseCode(SciploreResponseCode.PASSWORD_TOO_SHORT, "Please use a password with a minimum length of 6 characters.");
		
		if (!password.equals(retypedPassword)) 
			return new SciploreResponseCode(SciploreResponseCode.PASSWORDS_NOT_IDENTICAL, "The passwords you have entered are not identical. Please try again.");
				
		if(Contact.getUserContact(getSession(), eMail) != null)
			return new SciploreResponseCode(SciploreResponseCode.EMAIL_ALREADY_EXISTS, "This email address is already used by a user to register. Please use another one.");
		
		
		Session session = getSession();
		Person person;
		Contact contact = Contact.getContact(getSession(), eMail);

    	if(contact == null) {
    		person = new Person(this.getSession());
    		person.setNameFirst(firstName);
     		person.setNameMiddle(middleName);
     		person.setNameLast(lastName);
     		person.setDob(new GregorianCalendar(birthYear, 0, 1).getTime());		
     		if (male != null) {			
     			person.setGender((short) (male ? 1:0));
     		}
     		session.save(person);
     		
    		contact = new Contact(session, eMail, Contact.CONTACT_TYPE_PRIVATE_EMAIL);
    		contact.setPerson(person);
    		session.save(contact);
		}
    	// this can only happen if the person is not affiliated with any user (see "if"-statements above)
    	else {
    		person = contact.getPerson();
    		person.setNameFirst(firstName);
     		person.setNameMiddle(middleName);
     		person.setNameLast(lastName);
     		person.setDob(new GregorianCalendar(birthYear, 0, 1).getTime());		
     		if (male != null) {			
     			person.setGender((short) (male ? 1:0));
     		}
     		session.update(person);    		
    	}
		Collection<PersonHomonym> homonyms = person.getHomonyms();
		PersonHomonym personHomonym = null;
		PersonHomonym personHomonymEmpty = null;
    	if(homonyms != null) {
    		String nameComplete = PersonHomonym.createNameComplete(person);
    		
    		for (PersonHomonym homonym : homonyms) {
				if(homonym.getNameComparable() == null || homonym.getNameComparable().length() == 0) {
					personHomonymEmpty = homonym;
				}
				else if(homonym.getNameComparable().equals(nameComplete)) {
					personHomonym = homonym;
				} 
			}
    		
    		if (personHomonym == null) {
    			personHomonym = personHomonymEmpty;
    		}
    	}
    	if (personHomonym == null) {
    		personHomonym = new PersonHomonym(this.getSession());
    		personHomonym.setNameFirst(firstName);
    		personHomonym.setNameMiddle(middleName);
    		personHomonym.setNameLast(lastName);
    		personHomonym.setValid((short) 1);
    		personHomonym.setPerson(person);
    		session.save(personHomonym);
    	}
    	else {
    		if (personHomonym.getNameComparable() == null || personHomonym.getNameComparable().trim().length() == 0) {
        		personHomonym.setNameFirst(firstName);
        		personHomonym.setNameMiddle(middleName);
        		personHomonym.setNameLast(lastName);    		    	
        		session.update(personHomonym);
    		}
    	}
		
		this.setUsername(username);
		this.setPerson(person);
		this.setPassword(password, true);		
		this.setType(USER_TYPE_NORMAL);
		this.setLang(USER_DEFAULT_LANG);
		this.setRegistrationdate(new GregorianCalendar().getTime());
		this.setRegistrationIPAddress(remoteAddress);
		session.save(this);
		
		Newsletter newsletter = new Newsletter(this.getSession());
		newsletter.setUser(this);
		newsletter.setNewsGeneral(generalNewsLetter);
		newsletter.setNewsSearch(searchNewsLetter);
		newsletter.setNewsSplmm(splmmNewsLetter);
		session.save(newsletter);
		
		session.flush();
		
		return new SciploreResponseCode(SciploreResponseCode.OK, "User created.");
	}
	
	private  SciploreResponseCode createAnonymousUser(String username, String remoteAddress){		
		this.setUsername(username);		
		this.setPassword(null);	
		this.setLang(USER_DEFAULT_LANG);
		this.setRegistrationdate(new GregorianCalendar().getTime());
		this.setType(USER_TYPE_ANONYMOUS);
		this.setRegistrationIPAddress(remoteAddress);
		this.getSession().save(this);
		
		return new SciploreResponseCode(SciploreResponseCode.OK, "User created.");
	}
	
	private  SciploreResponseCode createExternalUser(String username, Short userType, String remoteAddress){		
		this.setUsername(username);		
		this.setPassword(null);	
		this.setLang(USER_DEFAULT_LANG);
		this.setRegistrationdate(new GregorianCalendar().getTime());
		this.setType(userType);
		this.setRegistrationIPAddress(remoteAddress);
		this.getSession().save(this);
		
		return new SciploreResponseCode(SciploreResponseCode.OK, "User created.");
	}


	/**
	 * @return the alterts
	 */
	public Set<Alert> getAlterts() {
		return alterts;
	}


	/**
	 * @return the feedbacks
	 */
	public Set<Feedback> getFeedbacks() {
		return feedbacks;
	}


	/**
	 * @return the lang
	 */
	public String getLang() {
		return lang;
	}


	/**
	 * @return the mindmaps
	 */
	public Set<Mindmap> getMindmaps() {
		return mindmaps;
	}


	/**
	 * @return the newsletters
	 */
	public Set<Newsletter> getNewsletters() {
		return newsletters;
	}


	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}


	/**
	 * @return the person
	 */
	public Person getPerson() {
		return person;
	}
	
	/**
	 * @return the registrationdate
	 */
	public Date getRegistrationdate() {
		return registrationdate;
	}


	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}


	/**
	 * @return the type
	 */
	public Short getType() {
		return type;
	}


	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}


	/**
	 * @return the valid
	 */
	public Short getValid() {
		return valid;
	}
	
	/**
	 * @return the user access token
	 */
	public String getAccessToken() {
		return accessToken;
	}


	public boolean isAllowBackup() {
		return allowBackup != null && allowBackup>0;
	}

	public void setAllowBackup(boolean allowBackup) {
		this.allowBackup = (short) (allowBackup ? 1 : 0);
	}

	public boolean isAllowContentResearch() {
		return allowContentResearch != null && allowContentResearch>0;
	}

	public void setAllowContentResearch(boolean allowContentResearch) {
		this.allowContentResearch = (short) (allowContentResearch ? 1 : 0);
	}

	public boolean isAllowInformationRetrieval() {
		return allowInformationRetrieval!=null && allowInformationRetrieval>0;
	}

	public void setAllowInformationRetrieval(boolean allowInformationRetrieval) {
		this.allowInformationRetrieval = (short) (allowInformationRetrieval ? 1 : 0);
	}

	public boolean isAllowUsageResearch() {
		return allowUsageResearch!=null && allowUsageResearch>0;
	}

	public void setAllowUsageResearch(boolean allowUsageResearch) {
		this.allowUsageResearch = (short) (allowUsageResearch ? 1 : 0);
	}

	public boolean isAllowRecommendations() {
		return allowRecommendations!=null && allowRecommendations>0;
	}

	public void setAllowRecommendations(boolean allowRecommendations) {
		this.allowRecommendations = (short) (allowRecommendations ? 1 : 0);
	}

	/**
	 * Checks if user data is correct 
	 * @param username
	 * @param password
	 * @return
	 */
	public boolean isValidUser(String username, String password){
		boolean isvalid=false;
		//check if user and passwort exist
		return isvalid;
		
	}


	/**
	 * @param alterts the alterts to set
	 */
	public void setAlterts(Set<Alert> alterts) {
		this.alterts = alterts;
	}


	/**
	 * @param feedbacks the feedbacks to set
	 */
	public void setFeedbacks(Set<Feedback> feedbacks) {
		this.feedbacks = feedbacks;
	}


	/**
	 * @param lang the lang to set
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}


	/**
	 * @param mindmaps the mindmaps to set
	 */
	public void setMindmaps(Set<Mindmap> mindmaps) {
		this.mindmaps = mindmaps;
	}


	/**
	 * @param newsletters the newsletters to set
	 */
	public void setNewsletters(Set<Newsletter> newsletters) {
		this.newsletters = newsletters;
	}


	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}


	public void setPassword(String password, Boolean crypt) {
		if(crypt) {
			this.password = crypt(password);
		} else {
			this.password = password;
		}
	}


	/**
	 * @param person the person to set
	 */
	public void setPerson(Person person) {
		this.person = person;
	}


	/**
	 * @param registrationdate the registrationdate to set
	 */
	public void setRegistrationdate(Date registrationdate) {
		this.registrationdate = registrationdate;
	}


	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(Short type) {
		this.type = type;
	}


	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param valid the valid to set
	 */
	public void setValid(Short valid) {
		this.valid = valid;
	}
	
	public void setAccessToken(String token) {
		this.accessToken = token;
	}
	
	public Resource getPersistentIdentity() {
		return getUser(this);
	}

	public String getRegistrationIPAddress() {
		return remote_address;
	}

	public void setRegistrationIPAddress(String remote_address) {
		this.remote_address = remote_address;
	}
}
