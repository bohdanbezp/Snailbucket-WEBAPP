<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<% if (pageContext.getSession().getAttribute("user") != null &&
		!DAO.playsInTl(UsefulMethods.getUsername(session))) { %>

<p>T45 will start on January 25th, 2010.</p>
<br/>

<form name="form1" id="form1" method="post" action="/actions/signtl">
How much time would you like to invest:
<select id="investtime" name="investtime">
<option value="0">I have so much time, I could even be regular player on one team and reserve player on a 2nd team.</option>
<option value="1">Lots of time, I might only miss two games or so.</option>
<option value="2">Not much time, but I can jump in as reserve player here and then.</option>
<option value="3">Sorry, there is no chance you will see me this tourney.</option>
</select><br/><br/>

You want to play in:<br/>
<select id="section" name="section">
<option value="Any section">Any section</option>
<option value="Open">Open</option>
<option value="u2000">u2000</option>
<option value="u1800">u1800</option>
<option value="u1600">u1600</option>
</select><br/>
<br/>
<input type="submit" value="Submit" />
</form>
<br/><br/>
<% }
else if (pageContext.getSession().getAttribute("user") == null) { %>
	<p><a href="/wiki/Special:Login" title="Special:Login">Log in</a> to register</p>
<% }
%>


<h2>Players who registered for T45</h2><br/>
<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#5e410f">
	       <td>Username</td>
	       <td>Fixed rating</td>
	       <td>Preferred section</td>
	       <td>Availability</td>
</tr> 
<%
out.print(DAO.getTlParticipantsTable());
%>
</table>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>
