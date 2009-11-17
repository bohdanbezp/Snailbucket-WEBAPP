package net.rwchess.site.data;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Key;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class UploadedFile implements Serializable {
	
	private static final long serialVersionUID = -6038369697729270999L;

	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;    
	
	@Persistent
	private String uploaderName;
	
	@Persistent
	private Date uploadDate;
	
	@Persistent
	private String description;
	
	@Persistent
	private String fileName;
	
	public UploadedFile(String uploaderName, Date uploadDate,
			String description, String file) {
		this.uploaderName = uploaderName;
		this.uploadDate = uploadDate;
		this.description = description;
		this.fileName = file;
	}

	public String getUploaderName() {
		return uploaderName;
	}

	public Date getUploadDate() {
		return uploadDate;
	}

	public String getDescription() {
		return description;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setUploaderName(String uploaderName) {
		this.uploaderName = uploaderName;
	}

	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setFileName(String file) {
		this.fileName = file;
	}
}
