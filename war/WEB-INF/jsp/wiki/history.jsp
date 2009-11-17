<%@ taglib uri="/WEB-INF/tags" prefix="rws" %>
<%@ page import="net.rwchess.wiki.*"%>

<% WikiPage wikiPage = (WikiPage) request.getAttribute("pageRequested"); %>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>RWarriors Wiki - <%=wikiPage.getName() %></title>
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />

</head>
<body>

<div id="wiki-page">
<div id="wiki-navigation">
	<div id="logo">
	
	<a class="logo" href="/wiki/en/StartingPoints"><img border="0" src="/wiki/images/logo_oliver.gif" alt="" /></a>
	</div>
	<br />
	
	<div id="nav-menu">
	<jsp:include page="/WEB-INF/jsp/wiki/leftMenu.jsp"></jsp:include>
	</div>
	
	<div id="nav-search">

	<form method="post" action="/wiki/en/Special:Search">
	<input type="text" name="text" size="20" value="" />
	<br />
	<input type="submit" name="search" value='Search'/>
	<input type="submit" name="jumpto" value='Jump to'/>
	</form>
	</div>
</div>
<div id="wiki-content">
	


<jsp:include page="/WEB-INF/jsp/wiki/reg.jsp"></jsp:include>

<div class="clear"></div>

	
<div id="tab-menu">	
	
</div>
<div class="clear"></div>

	<div id="contents" >
	
	<ul>
	<%
	  if (wikiPage.getHistory() == null) System.out.println("nukk");
	  for (String line: wikiPage.getHistory()) {
		  out.println("<li>"+line+"</li>");
	  }
	%>
	</ul>
	<br />
	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>


</body>
</html>