package net.rwchess.site.data;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import net.rwchess.site.utils.Base64Coder;

import com.google.appengine.api.datastore.Blob;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class File implements Serializable {
	
	private static final long serialVersionUID = -7536435941100546803L;

	@PrimaryKey
	@Persistent
	private String fileName;
	
	@Persistent
	private Blob file;
	
	public File(String fileName, Blob file) {
		this.fileName = fileName;
		this.file = file;
	}

	public String getFileName() {
		return fileName;
	}

	public Blob getFile() {
		return file;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFile(Blob file) {
		this.file = file;
	}
	
	public String toString() {
		return fileName + "\n\n" + Base64Coder.encodeLines(file.getBytes());
	}
}
