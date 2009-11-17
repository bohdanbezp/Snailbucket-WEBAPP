<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.RWMember"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>


<table border="1" align="center">
   <tr>
     <td>Username</td><td>Country</td><td>Group</td>
   </tr>
<%
 if (session.getAttribute("DeleteConfirmation") != null) {
	session.removeAttribute("DeleteConfirmation");
 }

 Object[] members = DAO.getAllPlayers();

 for (Object o: members) {
	 RWMember m = (RWMember) o;
%>
   <form name="form1" id="form1" method="post" action="/actions/admin/submembrs">
   <tr>
   <td><%=m.getUsername() %><input type="hidden" name="username" value="<%=m.getUsername() %>"/></td>
   <td><input type="text" name="country" maxlength="2" value="<%=m.getCountry() %>"/></td>
   <td><input type="text" name="group" maxlength="10" value="<%=UsefulMethods.groupToWord(m.getGroup()) %>"/></td>
   <td><input type="submit" name="submit" value="Submit"/></td>
   <td><a href="/actions/userdelete?username=<%=m.getUsername() %>">Delete user</a></td> 
   </tr> 
   </form>
<% } %>
</table>
<br/>

<br/>
<center><a href="/users/addmember.jsp">[Add member]</a></a></center>
<jsp:include page="/blocks/bottom.jsp"></jsp:include>