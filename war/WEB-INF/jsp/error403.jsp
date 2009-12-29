<%@ page import="net.rwchess.site.data.RWMember"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"/>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
	<title>Rainbow Warriors Bunch</title>
	<link rel="stylesheet" type="text/css" href="/static/style.css"/>
</head>
<% response.setStatus(403); %>
<body>
<div id="wrapper">
<div id="content">
<br/><br/><br/><br/><br/><br/><br/><br/><br/>
<center><h1>Forbidden</h1><br/>
<p>This error indicates either that you haven't permission to access the page or
that your permissions not high enough. If you are a member of Ranbow Warriors club
check that you are logged in with your username and password.</p><br/>

<% RWMember user = (RWMember) pageContext.getSession().getAttribute("user"); 
       if (user == null) {  
%>	
<jsp:include page="/users/login.jsp"></jsp:include>
<% } %>

</center>
</div>
</div>
</body>