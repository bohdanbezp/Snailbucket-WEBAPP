<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.RWMember"%>

<%
 Object[] members = DAO.getAllPlayers();
 int maxRows = members.length/4;
%>

<table border="0" align="center">
   <%
   int coloumn = 0;
   
   for (Object o: members) {
	   RWMember m = (RWMember) o;
	   if (coloumn == 0)
		   out.println("<tr>");
	   else if (coloumn == 3) {
		   out.println("</tr>");
		   coloumn = 0;
	   }
	   %>
	   <td width="25%">
	   <img src="http://simile.mit.edu/exhibit/examples/flags/images/<%=m.getCountry() %>.png" border="0"/>
	   <a href="/members/<%=m.getUsername() %>"><%=m.getUsername() %></a>
	   <%
	   coloumn++;
   }
   %>
</table>
