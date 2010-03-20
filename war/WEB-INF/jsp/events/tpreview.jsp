<%@ page import="net.rwchess.site.data.T42Player"%>
<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<h2>Players who registered for T42</h2><br/>
<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#5e410f">
	       <td>Username</td>
	       <td>Fixed rating</td>
	       <td>Preffered section</td>
	       <td>Availability</td>
</tr> 
<%
out.print(DAO.getTlParticipantsTable());
%>
</table>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>