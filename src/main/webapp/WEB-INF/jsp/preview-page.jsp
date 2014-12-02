<%@page pageEncoding="UTF-8" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - ${wikiPage.name}</title>
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
		var wgPageName = "${urlFriendlyName}";
		var wgTitle = "${wikiPage.name}";
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
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
   <style>
               #user-menu1 {
               	float: left;
               	margin-right: 2em;
               	padding: 0;
               	text-align: center;
               }
               #user-menu1 li {
               	display: inline;
               	list-style-type: none;
               	list-style-image: none;
               	padding: 0 0.8em;
               	font-size: 90%;
               }
             </style>
             <jsp:include page="tracking.jsp"></jsp:include>
</head>
<body>

<div id="wiki-page">
<div id="wiki-navigation">
	<div id="logo">

	<jsp:include page="logoArea.jsp"></jsp:include>
	</div>
	<br />

	<div id="nav-menu">
	<jsp:include page="leftMenu.jsp"></jsp:include>
	</div>

</div>
<div id="wiki-content">
    <div id="user-menu1">
             	<jsp:include page="top-menu.jsp"></jsp:include>
                 </div>
<jsp:include page="reg.jsp"></jsp:include>

<div class="clear"></div>


<div id="tab-menu">




	<div class="tab-item"><a href="/wiki/${urlFriendlyName}">Article</a></div>




	<div class="tab-item"><a href="/wiki/Special:Edit?page=${urlFriendlyName}"><b>Edit</b></a></div>



</div>
<div class="clear"></div>

	<div id="contents" >
	<h2 style="color:red" id="mw-previewheader">Preview edit</h2>


<div id="content-article">

${htmlText}

<div class="clear"></div>



	<br />


	<fieldset>
<legend>Editing Controls</legend>

<div id='toolbar'>
<script type='text/javascript'>

addButton("/static/images/wiki/button_bold.png","Bold text","\'\'\'","\'\'\'","Bold text","mw-editbutton-bold");
addButton("/static/images/wiki/button_italic.png","Italic text","\'\'","\'\'","Italic text","mw-editbutton-italic");
addButton("/static/images/wiki/button_link.png","Internal link","[[","]]","Link title","mw-editbutton-link");
addButton("/static/images/wiki/button_extlink.png","External link (remember http:// prefix)","[","]","http://www.example.com link title","mw-editbutton-extlink");
addButton("/static/images/wiki/button_headline.png","Level 2 headline","\n== "," ==\n","Headline text","mw-editbutton-headline");
addButton("/static/images/wiki/button_image.png","Embedded file","[[File:","]]","Example.jpg","mw-editbutton-image");
addButton("/static/images/wiki/button_nowiki.png","Ignore wiki formatting","\x3cnowiki\x3e","\x3c/nowiki\x3e","Insert non-formatted text here","mw-editbutton-nowiki");
addButton("/static/images/wiki/button_hr.png","Horizontal line (use sparingly)","\n----\n","","","mw-editbutton-hr");

</script>
</div>

<form name="form" method="post" name="editform" action="${nextPath}">
<input type="hidden" name="pageName" value="${wikiPage.name}" />



<p>
<textarea id="topicContents" name="contents" rows="25" cols="80" accesskey=",">${rawText}</textarea>
</p>
<p>

<input type="submit" name="save" value="Save"  accesskey="s" />

<input type="submit" name="save" value="Preview" accesskey="p" />


</p>



</form>

</fieldset>

	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>