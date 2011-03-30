<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<%
	if (!UsefulMethods.getUsername(session).equals("null")
			&& !DAO.playsInSwiss(UsefulMethods.getUsername(session))) {
%>
<p><b>Registration closed</b></p>
<%
	}
	else if (UsefulMethods.getUsername(session).equals("null")) {
%>
      
<%  } %>
<h2>Players registered for RW Swiss</h2><br/>
<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#5e410f">
		   <td WIDTH="6%" align="right">#</td>
	       <td WIDTH="33%" align="left">Username</td>
	       <td WIDTH="11%" align="center">Fixed <br/> rating</td>
	       <td WIDTH="6%" align="right">#</td>
	       <td WIDTH="33%" align="left">Username</td>
	       <td WIDTH="11%" align="center">Fixed <br/> rating</td>
</tr> 
<%
 	out.print(DAO.getSwissParticipantsTable());
 %>
</table>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>