<%@page pageEncoding="UTF-8" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - ${wikiPage.name}</title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
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


	<div class="tab-item"><a href="/wiki/Special:Edit?page=${urlFriendlyName}">Edit</a></div>
	<div class="tab-item"><a href="/wiki/Special:History?page=${urlFriendlyName}"><b>History</b></a></div>
</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">History ${wikiPage.name}</h1>



<div id="content-article">
	<ul>
	${historyList}
	</ul>
	<br />
	</div>
</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>


</body>
</html>
