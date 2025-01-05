<%@ page import="net.rwchess.persistent.Member"%>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%>
<%@ page import="net.rwchess.persistent.dao.TourneyDAOHib"%>
<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - Members</title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />

      <style>
      	#TourneyForm label.error {
      		color:#FF0000;
      	}
      	</style>
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

</div>
<div id="wiki-content">


<jsp:include page="reg.jsp"></jsp:include>

<div class="clear"></div>


<div id="tab-menu">





</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Members</h1>



<div id="content-article">
        ${membersTable}

<div class="clear"></div>



	<br />
	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>
