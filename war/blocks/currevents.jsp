<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.UploadedFile"%>

<div id="left">
<div class="leftitem">

<jsp:include page="/WEB-INF/jsp/wiki/reg.jsp"></jsp:include>

<ul>
	<h2>Current Events</h2>
	<ul>
		<li><a style="font-size: 15px;"
			href="/t41">T41</a></li>
	</ul>
	<ul>
		<li><a style="font-size: 15px;"
			href="/ladder">RW Ladder</a></li>
	</ul>
	</ul>
<br/>
<h2>Old events</h2>
<ul>
		<li><a style="font-size: 15px;"
			href="/swiss">RW Swiss</a></li>
	</ul><br/>
<h2>Latest activities</h2>
<% 
   Object[] files = DAO.getLatestUploadedFiles();
   for (Object o: files) {
	   UploadedFile file = (UploadedFile) o;
%>
   <p><i><a href="/members/<%=file.getUploaderName() %>"><%=file.getUploaderName() %></a> has uploaded file <%=file.getFileName() %></i></p>
<%} %>
</div>
</div>
<div id="right">
