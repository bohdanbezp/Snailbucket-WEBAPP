<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<% if (pageContext.getSession().getAttribute("user") != null &&
		!DAO.playsInTl(UsefulMethods.getUsername(session))) { %>

<p><b>T46</b><br>
preliminary: 7 weeks, April 26 - June 13<br>
playoffs: 1 week, June 14 - 20.</p>
<br/>

<form name="form1" id="form1" method="post" action="/actions/signtl">
How many games would you like to play?<br>
<select id="investtime" name="investtime">
<option value="0">1-3 games</option>
<option value="1">3-5 games</option>
<option value="2">5-8 games</option>
<option value="3">0 games :(</option>
</select><br/><br/>

Could you help as captain or deputy?<br/>
<select id="capt" name="capt">
<option value=" ">No, please not.</option>
<option value="Cap">Captain</option>
<option value="Dep">Deputy</option>
<option value="C/D">Captain or Deputy</option>
</select><br/>
<br/>

Optional: You can leave a <b>short</b> comment, stuff like "Not after May 24. Maybe available early in June, but it looks difficult."<br><input select id="comments" name="comments"><br><br>
<input type="submit" value="Submit" />
</form>
<br/><br/>
<% }
else if (pageContext.getSession().getAttribute("user") == null) { %>
	<p><a href="/wiki/Special:Login" title="Special:Login">Log in</a> to register</p>
<% }
%>


<h2>Players who registered for T46</h2><br/>
<table width="80%"  border="1" cellpadding="0" cellspacing="2">
<tr bgcolor="#6495ED">
	       <td>#</td>
	       <td>Username</td>
	       <td>Fixed<br>rating</td>
	       <td>Games</td>
	       <td>Cap/<br>Dep</td>
	       <td>Comments</td></tr> 
<%
out.print(DAO.getTlParticipantsTable());
%>
</table>
<br>
<p><a href="/wiki/User:bodzolca">bodzolca</a> will submit all teams on April 18. --> Please sign up before April 16! Any questions? Please contact bodzolca via email or fics message.</p>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>
