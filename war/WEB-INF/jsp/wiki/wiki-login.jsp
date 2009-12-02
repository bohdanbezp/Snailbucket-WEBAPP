<%@ page import="net.rwchess.wiki.*"%>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>RWarriors Wiki - Login</title>
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
	
<div class="clear"></div>

	
<div id="tab-menu">

	

</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Login</h1>
	


<div id="content-article">

<jsp:include page="/users/login.jsp"></jsp:include>

<div class="clear"></div>
	


	<br />
	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>