<%@page pageEncoding="UTF-8" %>
<%@ page import="net.rwchess.persistent.Member"%>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%>

<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - ${wikiPage.name}</title>
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
  <link href="/static/jquery-ui.css" rel="stylesheet">
    <script type="text/javascript" src="/static/jquery.js"></script>
    <script type="text/javascript" src="/static/jquery-ui.js"></script>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
   <script>
   $(document).ready(function(){
   $("#panel").slideDown("slow");
     $("#flip").click(function(){
       $("#panel").slideToggle("slow");
     });
   });
   </script>
<style>
#panel1,#flip
{
background-color:#e5eecc;
}
#panel1
{
padding:10px;
display:none;
}
</style>
<style>
        #sortable, #sortable2 { list-style-type: none; margin: 0; padding: 0; text-align: right; background: #eee; padding: 5px; width: 143px;}
          #sortable li, #sortable2 li { margin: 5px; padding: 5px; font-size: 1.2em; width: 120px; }

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
  <div class="clear"></div>

<jsp:include page="reg.jsp"></jsp:include>

<div class="clear"></div>

	
<div id="tab-menu">

	
	
	
	<div class="tab-item"><a href="/wiki/${urlFriendlyName}"><b>Article</b></a></div>

	
	
	
	<div class="tab-item"><a href="/wiki/Special:Edit?page=${urlFriendlyName}">Edit</a></div>
    <div class="tab-item"><a href="/wiki/Special:History?page=${urlFriendlyName}">History</a></div>


</div>
<div class="clear"></div>

	<div id="contents" >
    <% Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if (!(user instanceof String) && ((Member) user).getGroup() >= Member.TD) {
    %>
	<div align="right">
    	 <form action="/wiki/Special:Protect" method="post">
            <input type="hidden" name="pageName" value="${wikiPage.name }">
           <input type="submit" id="Login_0"
           			value="${tdProtectionText}" />
         </form>
         </div>
    <% } %>


	<h1 id="contents-header">${wikiPage.name }</h1>




<div id="content-article">
     <h3><noscript><font color="red">Please enable Javascript in order to have the best user experience with the website.</font></noscript></h3>
${toggleUser}

${wikiPage.htmlText}
	
<div class="clear"></div>
	


	<br />
	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>
