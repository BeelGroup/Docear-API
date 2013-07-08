package org.sciplore.beans;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.sciplore.annotation.SciBeanAlias;
import org.sciplore.annotation.SciBeanElements;
import org.sciplore.annotation.SciBeanImplicitValue;
import org.sciplore.formatter.Bean;
import org.sciplore.formatter.SimpleTypeElementBean;

@SciBeanAlias("version")
@SciBeanElements({ "release_date", "build", "major", "middle", "minor", "status", "status_number", "release_note" })
public class ApplicationVersion extends Bean {

	private String id;
	private String href;

	private Bean release_date;
	private Bean build;
	private Bean major;
	private Bean middle;
	private Bean minor;
	private Bean status;
	private Bean status_number;
	private Bean release_note;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public Bean getRelease_date() {
		return release_date;
	}

	public void setRelease_date(Date release_date) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
		this.release_date = new SimpleValueBean(dateFormatter.format(release_date));
		activateElement("release_date");
	}

	public Bean getBuild() {
		return build;
	}

	public void setBuild(Integer build) {
		this.build = new SimpleValueBean(build);
		activateElement("build");
	}

	public Bean getMajor() {
		return major;
	}

	public void setMajor(Integer major) {
		this.major = new SimpleValueBean(major);
		activateElement("major");
	}

	public Bean getMiddle() {
		return middle;
	}

	public void setMiddle(Integer middle) {
		this.middle = new SimpleValueBean(middle);
		activateElement("middle");
	}

	public Bean getMinor() {
		return minor;
	}

	public void setMinor(Integer minor) {
		this.minor = new SimpleValueBean(minor);
		activateElement("minor");
	}

	public Bean getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = new SimpleValueBean(status);
		activateElement("status");
	}

	public Bean getStatus_number() {
		return status_number;
	}

	public void setStatus_number(Integer status_number) {
		this.status_number = new SimpleValueBean(status_number);
		activateElement("status_number");
	}

	public Bean getRelease_note() {
		return release_note;
	}

	public void setRelease_note(String release_note) {
		this.release_note = new SimpleValueBean(release_note);
		activateElement("release_note");
	}
	
	@SciBeanImplicitValue("value")
	class SimpleValueBean extends SimpleTypeElementBean {
		public SimpleValueBean() {
			super();
		}
		
		public SimpleValueBean(Object value) {
			super();
			if(value == null) {
				setValue("");
			}
			else {
				setValue(value.toString());
			}
		}
	}

}
