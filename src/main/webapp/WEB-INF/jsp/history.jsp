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
                  #panel1, #flip {
                      background-color: #e5eecc;
                  }
                  #panel1 {
                      padding: 10px;
                      display: none;
                  }
                  #sortable, #sortable2 {
                      list-style-type: none;
                      margin: 0;
                      padding: 0;
                      text-align: right;
                      background: #eee;
                      padding: 5px;
                      width: 143px;
                  }
                  #sortable li, #sortable2 li {
                      margin: 5px;
                      padding: 5px;
                      font-size: 1.2em;
                      width: 120px;
                  }
                  #user-menu1 {
                      float: left;
                      margin-right: 2em;
                      padding: 0;
                      text-align: center;
                      height: 20px; /* Adjusted height */
                      overflow: hidden; /* Prevents increasing overall size */
                  }
                  #user-menu1 li {
                      display: inline;
                      list-style-type: none;
                      list-style-image: none;
                      padding: 5px 0.8em 0 0.8em; /* Added top padding */
                      font-size: 100%; /* Increased font size */
                  }
                  /* Added CSS for login text */
                  .login-text {
                      font-size: 100%; /* Increased font size */
                  }
                  /* Existing responsiveness CSS */
                  #wiki-content {
                      margin: 0 auto;
                      max-width: 1300px;
                      padding: 0 15px;
                  }
                  @media screen and (max-width: 600px) {
                      #wiki-content {
                          padding: 0 10px;
                          max-width: none;
                      }
                  }
                      #user-menu1 ul {
                        list-style: none;
                        margin: 0;
                        padding: 0;
                      }
                      #user-menu1 ul li {
                        position: relative;
                        display: inline-block;
                      }
                      #user-menu1 ul li a {
                        text-decoration: none;
                        display: block;
                        padding: 10px;
                        color: #000;
                      }
                      #user-menu1 ul li ul {
                        display: none;
                        position: absolute;
                        background-color: #e5eecc;
                        min-width: 160px;
                        list-style: none;
                        padding: 0;
                        margin: 0;
                      }
                      #user-menu1 ul li:hover > ul {
                        display: block;
                      }
                      #user-menu1 ul li ul li {
                        display: block;
                      }
                      #user-menu1 ul li ul li a {
                        padding: 10px;


                      #user-menu1 ul li ul li a:hover {
                        background-color: #ddd;
                      }
                      #tab-menu .tab-item {
                                      	float: left;
                                      	background: white;
                                      	color: blue;
                                      	border: 1px solid #D8D8E7;
                                      	border-bottom: none;
                                      	padding: 0.2em 0.5em 0.2em 0.5em;
                                      	margin: 0;
                                      	margin-right: 6px;
                                      	font-size: 125%;
                                      	text-align: center;
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
