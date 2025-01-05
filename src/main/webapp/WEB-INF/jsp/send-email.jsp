<%@ page import="net.rwchess.wiki.*"%>
<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - Send Email</title>
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
		var wgTitle = "${wikiPage}";
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
                    #panel1, #flip {
                        background-color: #e5eecc;
                    }
                    #panel1 {
                        padding: 10px;
                        display: none;
                    }
                    #sortable, #sortable2 {
                        list-style-type: none;
                        margin: 0;
                        padding: 0;
                        text-align: right;
                        background: #eee;
                        padding: 5px;
                        width: 143px;
                    }
                    #sortable li, #sortable2 li {
                        margin: 5px;
                        padding: 5px;
                        font-size: 1.2em;
                        width: 120px;
                    }
                    #user-menu1 {
                        float: left;
                        margin-right: 2em;
                        padding: 0;
                        text-align: center;
                        height: 20px; /* Adjusted height */
                        overflow: hidden; /* Prevents increasing overall size */
                    }
                    #user-menu1 li {
                        display: inline;
                        list-style-type: none;
                        list-style-image: none;
                        padding: 5px 0.8em 0 0.8em; /* Added top padding */
                        font-size: 100%; /* Increased font size */
                    }
                    /* Added CSS for login text */
                    .login-text {
                        font-size: 100%; /* Increased font size */
                    }
                    /* Existing responsiveness CSS */
                    #wiki-content {
                        margin: 0 auto;
                        max-width: 1300px;
                        padding: 0 15px;
                    }
                    @media screen and (max-width: 600px) {
                        #wiki-content {
                            padding: 0 10px;
                            max-width: none;
                        }
                    }
                        #user-menu1 ul {
                          list-style: none;
                          margin: 0;
                          padding: 0;
                        }
                        #user-menu1 ul li {
                          position: relative;
                          display: inline-block;
                        }
                        #user-menu1 ul li a {
                          text-decoration: none;
                          display: block;
                          padding: 10px;
                          color: #000;
                        }
                        #user-menu1 ul li ul {
                          display: none;
                          position: absolute;
                          background-color: #e5eecc;
                          min-width: 160px;
                          list-style: none;
                          padding: 0;
                          margin: 0;
                        }
                        #user-menu1 ul li:hover > ul {
                          display: block;
                        }
                        #user-menu1 ul li ul li {
                          display: block;
                        }
                        #user-menu1 ul li ul li a {
                          padding: 10px;
                        }
                        #user-menu1 ul li ul li a:hover {
                          background-color: #ddd;
                        }
                </style>
</head>
<body>

<div id="wiki-page">
<div id="wiki-navigation">


</div>
<div id="wiki-content">

    <jsp:include page="reg.jsp"></jsp:include>

    <div class="clear"></div>

    <div id="tab-menu">
        <div class="tab-item"><a href="/wiki/${urlFriendlyName}">Article</a></div>
        <div class="tab-item"><a href="/wiki/Special:Edit?page=${urlFriendlyName}"><b>Edit</b></a></div>
        <div class="tab-item"><a href="/wiki/Special:History?page=${urlFriendlyName}">History</a></div>
    </div>
    <div class="clear"></div>

    <div id="contents" >
        <h1 id="contents-header">Send Email from tds@snailbucket.org</h1>
        <form action="${pageContext.request.contextPath}/email/send" method="post">
            <label for="recipients">Recipients (comma separated): </label><br/>
            <input type="text" id="recipients" name="recipients" required>
            <br><br>
            <label for="subject">Subject: </label><br/>
            <input type="text" id="subject" name="subject" required>
            <br><br>
            <label for="message">Message:</label><br/>
            <textarea id="message" name="message" rows="10" cols="30" required></textarea>
            <br><br>
            <input type="submit" value="Send Email">
        </form>
    </div>
</div>

<jsp:include page="footer.jsp"></jsp:include>

</div>

</body>
</html>
