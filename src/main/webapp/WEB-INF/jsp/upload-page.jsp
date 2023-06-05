<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - Upload file</title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
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





</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Upload file</h1>



<div id="content-article">

<p>You can upload a file; adding a description will help the admins. It
might be anything chess-related or interesting for Snail Bucket members.
Please don't violate copyright and other laws. </p>

<p>Embed images into wiki pages using syntax like [[File:filename.png]].
Access images through <a href="/wiki/Special:ImageRegistry"
title="Special:ImageRegistry">Uploaded images</a>.
Access files through /wiki/files/filename.  </p>

<br/>

<form name="filesForm" action="/wiki/Special:Upload" method="post" enctype="multipart/form-data">
<input type="file" name="downldFile"><br/><br/>
<textarea name="description" cols="54" rows="6" tabindex="3"></textarea>
<br/><br/>
<input type="submit" name="Submit" value="Upload File">
</form>

<div class="clear"></div>



	<br />
	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>
