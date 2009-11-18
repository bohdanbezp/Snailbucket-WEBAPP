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

	
	
	
	<div class="tab-item"><a href="/wiki/<%=wikiPage.getName().replace(' ', '_') %>">Article</a></div>

	
	
	
	<div class="tab-item"><a href="/wiki/Special:Edit?page=<%=wikiPage.getName().replace(' ', '_') %>">Edit</a></div>
    <div class="tab-item"><a href="/wiki/Special:History?page=<%=wikiPage.getName().replace(' ', '_') %>">History</a></div>
	

</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header"><%=wikiPage.getName() %></h1>
	


<div id="content-article">

<%=wikiPage.getHtmlText().getValue() %>		
	
<div class="clear"></div>
	


	<br />
	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>