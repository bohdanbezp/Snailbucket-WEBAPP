<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.SwissGuest"%>
<%@ page import="java.util.List;"%>
<%@ page import="net.rwchess.site.data.RWMember"%>
<%@ page import="net.rwchess.site.utils.UsernameComparable"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<% UsernameComparable user = (UsernameComparable) pageContext.getSession().getAttribute("user"); 
       if (user != null) {  
       		if (user instanceof RWMember && ((RWMember) user).getGroup() >= RWMember.TD) {
%>

<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#5e410f" align="center">
	       <td>Username</td>
	       <td>Email</td>
	       <td>Generated password</td>
	       <td>Approved</td>
</tr> 
<%
 	List<SwissGuest> guests = DAO.getSwissGuests();
 	for (SwissGuest g: guests) {
 		out.print("<tr align=\"center\">");
 		out.print("<td>"+g.getUsername()+"</td>");
 		out.print("<td>"+g.getEmail()+"</td>");
 		
 		if (!g.isConfirmed())
 			out.print("<td>"+g.getGeneratedPlainPassword()+"</td>");
 		else
 			out.print("<td>Approved</td>");	
 		
 		out.print("<td>"+(g.isConfirmed()?"Yes":"<b>No</b>")+"</td>");
 		out.print("</tr>");
 	}
 	
 	}
 	else {
 %>
 		<p>Sorry, your permissions aren't high enough to view the page</p>
 <% }%>
</table>

<% }
else {  %>
	<p>If you're TD or Moderator <a href="/wiki/Special:Login" title="Special:Login">log in</a> to view the page</p>
<%}
%>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>