<%@ taglib uri="/WEB-INF/tags" prefix="rws" %>
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

	

</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Login</h1>
	


<div id="content-article">

<form action="/wiki/Special:Login" method="post">
<table class="wwFormTable">
	<tr>
		<td class="tdLabel"><label for="Login_username" class="label">Username:</label></td>

		<td><input type="text" name="login" value=""
			id="Login_username" /></td>
	</tr>

	<tr>
		<td class="tdLabel"><label for="Login_password" class="label">Password:</label></td>
		<td><input type="password" name="password" id="Login_password" /></td>
	</tr>

	<tr>
		<td colspan="2">
		<div align="right"><input type="submit" id="Login_0"
			value="Submit" /></div>
		</td>
	</tr>

</table>    
</form>

<div class="clear"></div>
	


	<br />
	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>