<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.UploadedFile"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<% String username = request.getRequestURI();   
   if (username.length() <= 8) {    
    %>
	   <jsp:include page="/users/members.jsp"></jsp:include>
  <% }
   else {
	   username = username.substring(9);
%>

<p>Uploaded files:</p><br/>

<% Object[] files = DAO.getUploadedFiles(username);
   for (Object o: files) {
	   UploadedFile file = (UploadedFile) o;
%>

<p><img src="http://upload.wikimedia.org/wikipedia/commons/thumb/8/80/38254-new_folder-12.svg/30px-38254-new_folder-12.svg.png" border="0"/>
<a href="/files/<%=file.getFileName() %>"><%=file.getFileName() %> </a>
<i><%=file.getUploadDate() %></i>
<br/>
<i><%=file.getDescription() %></i>
</p>
<br/>

<% } 
   }
%>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>