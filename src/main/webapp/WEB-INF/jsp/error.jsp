<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - ${title}</title>
  <meta name="robots" content="noindex,nofollow" />
  <script type="text/javascript" src="/static/jquery-latest.js"></script>

  	<script type="text/javascript" src="/static/jquery.tablesorter.js"></script>
  	<script type="text/javascript" src="/static/jquery.tablesorter.pager.js"></script>
  	<script type="text/javascript" src="/static/chili-1.8b.js"></script>
  	<script type="text/javascript" src="/static/docs.js"></script>
  	<script type="text/javascript" src="/static/examples.js"></script>
  	<script type="text/javascript" id="js">$(document).ready(function() {
  	$("table").tablesorter();
  });</script>

  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
  <link rel="stylesheet" href="/static/style2.css" type="text/css" media="print, projection, screen" />
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





</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">${title}</h1>



<div id="content-article">


${error}

<div class="clear"></div>



	<br />
	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>