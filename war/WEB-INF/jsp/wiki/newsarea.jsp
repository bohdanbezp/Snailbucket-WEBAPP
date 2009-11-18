<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>
<%@ page import="net.rwchess.site.data.ForumMessage"%>
<%@ page import="java.util.List"%>
<% 

Object[] results = DAO.getNewsList(5);
		
for (Object o : results) { 	
	ForumMessage m = (ForumMessage) o;   	

%>
<h2><%=m.getTitle() %>!</h2>
<br />
<%=UsefulMethods.parseNewsText(m.getMessage().getValue()) %>
<br />
<b>Posted by&nbsp;<%=m.getUsername() %> on <%=m.getTimestamp() %></b>

<% 	}
%>
<br/>
<br/>
<center><a href="/users/postmessage.jsp">[Post message]</a></a></center>

