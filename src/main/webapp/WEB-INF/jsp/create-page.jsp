<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - Create page</title>
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
          	<ul>

              <li><a href="/members">Members</a></li>
              <li><a href="/wiki/Links">Links</a></li>
                <li><a href="/wiki/Wiki-Howto">Wiki Howto</a></li>
                <li><a href="/wiki/Trivia">Trivia</a></li>
                <li><a href="/wiki/FAQ">FAQ</a></li>
                 <li><a href="/wiki/Contact">Contact</a></li>
              </ul>
              </div>

<jsp:include page="reg.jsp"></jsp:include>

<div class="clear"></div>


<div id="tab-menu">





</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Create page</h1>



<div id="content-article">

<form action="/wiki/Special:Create" method="get">
<tr>
	<td class="tdLabel"><label for="page" class="label">Page name:</label></td>

	<td><input type="text" name="page" value="" id="page" /></td>
	</tr>
	<input type="submit" id="Login_0"
			value="Submit" />
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