<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>
<%@ page import="net.rwchess.site.data.ForumMessage"%>
<%@ page import="net.rwchess.site.data.RWMember"%>
<%@ page import="java.util.List"%>
<% 

Object[] results = DAO.getNewsList(5);

for (Object o : results) { 	
	ForumMessage m = (ForumMessage) o;   	

%>
<h2><%=m.getTitle() %></h2>
<br />
<%=UsefulMethods.parseNewsText(m.getMessage().getValue()) %>
<br />
<small><b>Posted by&nbsp;<%=m.getUsername() %> on <%=m.getTimestamp() %></b></small>
<br/>
<br/>
<% 	}
%>

<% RWMember user = (RWMember) pageContext.getSession().getAttribute("user"); 
       if (user != null) {  
%>	
<center><a href="/users/postmessage.jsp">[Post message]</a></center>
<% } %>
