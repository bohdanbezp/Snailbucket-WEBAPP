<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>
<%@ page import="net.rwchess.site.data.TeamDuel"%>
<%@ page import="net.rwchess.site.data.T41Player"%>
<%@ page import="java.util.List"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<small>More information on <b></b><a href="/wiki/T41">wiki</a></b></small><br/><br/>

    
    <br/> 
	<h2>Most successful players</h2></br> 
    <table width="80%"  border="0" cellpadding="0" cellspacing="2">
<%  int i = 1;
    for (T41Player pl: DAO.getTlParticipants(true)) { %>
    	<tr>
    	<% 
    	out.println("<td width=\"5%\">"+i++ + "</td>");
    	out.println("<td>"+pl.getUsername()+"</td>");
    	out.println("<td>"+pl.getPoints()+"/"+pl.getGames()+"</td>");
    	%>
    	</tr>
    	<%
    }
%> 
    </table>


<jsp:include page="/blocks/bottom.jsp"></jsp:include>
