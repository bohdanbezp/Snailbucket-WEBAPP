package net.rwchess.wiki;

import java.io.Serializable;
import java.util.Stack;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import net.rwchess.site.utils.UsefulMethods;

import com.google.appengine.api.datastore.Text;

@PersistenceCapable(identityType = IdentityType.DATASTORE)
public class WikiPage implements Serializable {
	
	private static final long serialVersionUID = 6506343119392793016L;

	@PrimaryKey
	@Persistent
	private String name;
	
	@Persistent
	private Text rawText;
	
	@Persistent
	private Text htmlText;
	
	@Persistent
	private Stack<String> history;

	public String getName() {
		return name;
	}

	public Text getRawText() {
		return rawText;
	}

	public Text getHtmlText() {
		return htmlText;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRawText(Text rawText) {
		this.rawText = rawText;
		this.htmlText = UsefulMethods.getHtml(rawText);
	}

	public void setHtmlText(Text htmlText) {
		this.htmlText = htmlText;
	}
	
	public Stack<String> getHistory() {
		if (history == null) history = new Stack<String>();
		return history;
	}

	public void setHistory(Stack<String> history) {
		this.history = history;
	}
}
