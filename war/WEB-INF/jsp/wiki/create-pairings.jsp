<%@ page import="net.rwchess.site.servlets.CreatePairings"%>
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
<jsp:include page="/WEB-INF/jsp/wiki/reg.jsp"></jsp:include>	
<div class="clear"></div>

	
<div id="tab-menu">

	

</div>
<div class="clear"></div>

	<div id="contents" >
	


<div id="content-article">

<form name="form" method="post" name="editform"
	action="/actions/crpairings">

<table>
	<tr>
		<td class="tdLabel"><label for="round" class="label">Round:</label></td>
		<td><input type="round" name="round" id="round" /></td>
	</tr>

	<tr>
		<td colspan="2"><input type="submit" name="button"
			value="Create game forums" accesskey="s" /></td>
	</tr>
</table>
</form>

<p>
<%=CreatePairings.getPairingsFromSource() %>

</p>

<div class="clear"></div>
	


	<br />
	</div>
</div>
<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>