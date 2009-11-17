package net.rwchess.site.tags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import net.rwchess.site.data.RWMember;

public class UserCheckTag extends SimpleTagSupport {
	
	private String status;
	private String checkFor;
	
	public void doTag() throws JspException, IOException {
		PageContext pageContext = (PageContext)getJspContext();
		RWMember user = (RWMember) pageContext.getSession().getAttribute("user");
				
		if (checkFor.equals("equality")) {
			if (user != null) {			
				if (isCorrespondingStatus(user)) 
					getJspBody().invoke(getJspContext().getOut());
			}
			else if (status.equals("anonymous"))
				getJspBody().invoke(getJspContext().getOut());
		}
		else {
			if (user != null) {			
				if (!isCorrespondingStatus(user)) 
					getJspBody().invoke(getJspContext().getOut());
			}
		}
			
	}

	private boolean isCorrespondingStatus(RWMember user) {
		switch (user.getGroup()) {
		  case RWMember.MEMBER:
			  return status.equals("member");
		  case RWMember.TD:
			  return status.equals("td");
		  case RWMember.MODERATOR:
			  return status.equals("moderator");
		  case RWMember.ADMIN:
			  return status.equals("admin");		 	  
		}
		return false;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCheckFor() {
		return checkFor;
	}

	public void setCheckFor(String checkFor) {
		this.checkFor = checkFor;
	}
}
