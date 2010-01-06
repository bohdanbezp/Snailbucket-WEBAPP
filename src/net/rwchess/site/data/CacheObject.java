package net.rwchess.site.data;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class CacheObject {
	
	@PrimaryKey
	@Persistent
	private String key;
	
	@Persistent
	private Text html;

	public String getKey() {
		return key;
	}

	public Text getHtml() {
		return html;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setHtml(Text html) {
		this.html = html;
	}
}
