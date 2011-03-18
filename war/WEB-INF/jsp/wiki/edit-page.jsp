<%@ page import="net.rwchess.wiki.*"%>

<% WikiPage wikiPage = (WikiPage) request.getAttribute("pageRequested"); %>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>RWarriors Wiki - <%=wikiPage.getName() %></title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />  
  
  		<script type= "text/javascript">
		var skin = "monobook";
		var stylepath = "/mediawiki/skins";
		var wgArticlePath = "/w/$1";
		var wgScriptPath = "/mediawiki";
		var wgScript = "/mediawiki/index.php";
		var wgVariantArticlePath = false;
		var wgActionPaths = {};
		var wgServer = "http://wiki.vuze.com";
		var wgCanonicalNamespace = "";
		var wgCanonicalSpecialPageName = false;
		var wgNamespaceNumber = 0;
		var wgPageName = "<%=wikiPage.getName().replace(' ', '_')%>";
		var wgTitle = "<%=wikiPage.getName()%>";
		var wgAction = "edit";
		var wgArticleId = "3691";
		var wgIsArticle = false;
		var wgUserName = null;
		var wgUserGroups = null;
		var wgUserLanguage = "en";
		var wgContentLanguage = "en";
		var wgBreakFrames = false;
		var wgCurRevisionId = 8969;
		var wgVersion = "1.15.1";
		var wgEnableAPI = true;
		var wgEnableWriteAPI = true;
		var wgSeparatorTransformTable = ["", ""];
		var wgDigitTransformTable = ["", ""];
		var wgRestrictionEdit = [];
		var wgRestrictionMove = [];</script>

  <script type="text/javascript" src="/static/wikibits.js"></script>
 
  <script type="text/javascript" src="/static/edit.js"></script>
  
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
	<h1 id="contents-header"><%=wikiPage.getName() %></h1>
	



<fieldset>
<legend>Editing Controls</legend>

<div id='toolbar'>
<script type='text/javascript'>

addButton("http://www.torak.info/mediawiki/skins/common/images/button_bold.png","Bold text","\'\'\'","\'\'\'","Bold text","mw-editbutton-bold");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_italic.png","Italic text","\'\'","\'\'","Italic text","mw-editbutton-italic");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_link.png","Internal link","[[","]]","Link title","mw-editbutton-link");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_extlink.png","External link (remember http:// prefix)","[","]","http://www.example.com link title","mw-editbutton-extlink");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_headline.png","Level 2 headline","\n== "," ==\n","Headline text","mw-editbutton-headline");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_image.png","Embedded file","[[File:","]]","Example.jpg","mw-editbutton-image");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_nowiki.png","Ignore wiki formatting","\x3cnowiki\x3e","\x3c/nowiki\x3e","Insert non-formatted text here","mw-editbutton-nowiki");
addButton("http://www.torak.info/mediawiki/skins/common/images/button_hr.png","Horizontal line (use sparingly)","\n----\n","","","mw-editbutton-hr");

</script>
</div>

<form name="form" method="post" name="editform" action="/wiki/Special:Edit">
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



	<br />
	</div>
</div>

<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>

</div>


</body>
</html>
