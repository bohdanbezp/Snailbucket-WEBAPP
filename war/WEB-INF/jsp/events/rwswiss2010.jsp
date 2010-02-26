<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<%
	if (!UsefulMethods.getUsername(session).equals("null")
			&& !DAO.playsInSwiss(UsefulMethods.getUsername(session))) {
%>
<p>Please read the <a href="/wiki/RW_Swiss">tourney guide</a> before registering.</p>
<form name="form1" id="form1" method="post" action="/actions/signswiss">
      <br/>
      <input type="submit" value="Sign up" />
      </form>
      <br/>
<%
	}
	else if (UsefulMethods.getUsername(session).equals("null")) {
%>
      <p><a href="/wiki/Special:Login">Log in</a> in order to sign up for RW Swiss 2010</p>
      <br/> 
<%  } %>
<h2>Players registered for RW Swiss</h2><br/>
<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#5e410f">
	       <td>Username</td>
	       <td>Fixed rating</td>
</tr> 
<%
 	out.print(DAO.getSwissParticipantsTable());
 %>
</table>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>