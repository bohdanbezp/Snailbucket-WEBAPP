<%@ page import="java.util.List"%>
<%@ page import="java.util.Stack"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.LatestEvents"%>

<div id="left">
<div class="leftitem">

<jsp:include page="/WEB-INF/jsp/wiki/reg.jsp"></jsp:include>

<ul>
	<h2>Current Events</h2>
        <ul>
		<li><a style="font-size: 15px;"
			href="/wiki/RW_Swiss">RW Swiss 2010</a></li>
	</ul>
	<ul>
		<li><a style="font-size: 15px;"
			href="/t42">T42</a></li>
	</ul>
	</ul>
<br/>
<h2>Blogs</h2>
<ul>
		<%=DAO.getAliveUsersTable()%>
</ul>
	<br/>
<h2>Latest activities</h2>
<ul>
<% LatestEvents ev = DAO.getEvents();   
   if (ev != null) {
	   for (String s: ev.getStack()) {
		   out.println("<li>"+s+"</li><br/>");
	   }
   }
%>
</ul>
</div>
</div>
<div id="right">
