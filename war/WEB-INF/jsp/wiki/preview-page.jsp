<%@ page import="net.rwchess.wiki.*"%>

<% WikiPage wikiPage = (WikiPage) request.getAttribute("pageRequested");  

   String postPage;
   if ((Boolean) request.getAttribute("previewCreate"))
	   postPage = "/wiki/Special:Create";
   else
	   postPage = "/wiki/Special:Edit";
%>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>RWarriors Wiki - <%=wikiPage.getName() %></title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />

</head>
<body>

<div id="wiki-page">
<div id="wiki-navigation">
	<div id="logo">
	
	<jsp:include page="/WEB-INF/jsp/wiki/logoArea.jsp"></jsp:include>
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

	
	
	
	<div class="tab-item"><a href="/wiki/Special:Edit?page=<%=wikiPage.getName().replace(' ', '_') %>" class="edit">Edit</a></div>

	

</div>
<div class="clear"></div>

	<div id="contents" >
	<h2 style="color:red" id="mw-previewheader">Preview edit</h2>


<div id="content-article">

<%=wikiPage.getHtmlText().getValue() %>		
	
<div class="clear"></div>
	


	<br />
	
	
	<fieldset>
<legend>Editing Controls</legend>

<form name="form" method="post" name="editform" action="<%=postPage %>">
<input type="hidden" name="pageName" value="<%=wikiPage.getName() %>" />



<p>
<textarea id="topicContents" name="contents" rows="25" cols="80" accesskey=","><%=wikiPage.getRawText().getValue() %></textarea>
</p>
<p>

<input type="submit" name="save" value="Save"  accesskey="s" />

<input type="submit" name="preview" value="Preview" accesskey="p" />


</p>



</form>

</fieldset>

	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>