<%@ page import="net.rwchess.wiki.*"%>
<%@ page import="net.rwchess.site.utils.UsernameComparable"%>
<%@ page import="net.rwchess.site.utils.*"%>

<% WikiPage wikiPage = (WikiPage) request.getAttribute("pageRequested"); %>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>RWarriors Wiki - <%=wikiPage.getName() %></title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
  <link rel="alternate" type="application/rss+xml" title="<%=wikiPage.getName() %>" href="/rss.xml"/>
</head>
  

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
	<h1 id="contents-header"><%=UsefulMethods.convertSwissName(wikiPage.getName()) %> <a href="/wiki/<%=wikiPage.getName() %>/rss.xml">RSS</a><img src="http://upload.wikimedia.org/wikipedia/commons/thumb/4/43/Feed-icon.svg/18px-Feed-icon.svg.png"/></h1>
	

<%=wikiPage.getHtmlText().getValue() %>

<% UsernameComparable user = (UsernameComparable) pageContext.getSession().getAttribute("user"); 
   if (user != null) {  
%>	
<fieldset>

<form name="form" method="post" name="editform" action="/wiki/<%=wikiPage.getName() %>">
<input type="hidden" name="pageName" value="<%=wikiPage.getName() %>" />



<p>
<textarea id="topicContents" name="contents" rows="25" cols="10" accesskey=","></textarea>
</p>
<p>

<div align="center"><table><tbody><tr><td>MM</td><td>DD</td><td>HH</td><td>mm</td></tr><tr><td><select name="month" cols="3"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option></select></td><td><select name="day"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option><option value="24">24&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="26">26&nbsp;&nbsp;</option><option value="27">27&nbsp;&nbsp;</option><option value="28">28&nbsp;&nbsp;</option><option value="29">29&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="31">31&nbsp;&nbsp;</option></select></td><td><select name="hour"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option></select></td><td><select name="minute"><option value="0">00&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="35">35&nbsp;&nbsp;</option><option value="40">40&nbsp;&nbsp;</option><option value="45">45&nbsp;&nbsp;</option><option value="50">50&nbsp;&nbsp;</option><option value="55">55&nbsp;&nbsp;</option></select></td></tr></tbody></table><br/><input name="submit" value="Set Time" type="submit"></div>

<input type="submit" name="save" value="Post"  accesskey="s" />


</p>



</form>

</fieldset>
<% }
else {
 %>
 <br/>
 <p>Please <a href="/wiki/Special:Login">log in</a> in order to post in the game forum.</p>
<% } %>



	<br />
	</div>
</div>

<jsp:include page="/WEB-INF/jsp/wiki/footer.jsp"></jsp:include>

</div>


</body>
</html>
