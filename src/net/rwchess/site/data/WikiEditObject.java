package net.rwchess.site.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class WikiEditObject implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Persistent
	private long dateStamp;
	
	@Persistent
	private Text originalVersion;
	
	@Persistent
	private Text changedVersion;
	
	@PrimaryKey
	@Persistent
	private String pageName;
	
	@Persistent
	private String uname;

	public long getDateStamp() {
		return dateStamp;
	}

	public Text getOriginalVersion() {
		return originalVersion;
	}

	public Text getChangedVersion() {
		return changedVersion;
	}

	public String getPageName() {
		return pageName;
	}

	public void setDateStamp(long dateStamp) {
		this.dateStamp = dateStamp;
	}

	public void setOriginalVersion(Text originalVersion) {
		this.originalVersion = originalVersion;
	}

	public void setChangedVersion(Text changedVersion) {
		this.changedVersion = changedVersion;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getUname() {
		return uname;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}
}
