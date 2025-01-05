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
  	<script type="text/javascrformipt" src="/static/examples.js"></script>
  	<script type="text/javascript" id="js">$(document).ready(function() {
  	$("table").tablesorter();
  });</script>
<script>
   function setType(type)
   {
      //formName is the name of your form, submitType is the name of the submit button.
      document.forms["form"].elements["name"].value = type;
   }
</script>

  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
  <link rel="stylesheet" href="/static/style2.css" type="text/css" media="print, projection, screen" />
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
                     }
                     #user-menu1 ul li ul li a:hover {
                       background-color: #ddd;
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
	<h1 id="contents-header">${title}</h1>



<div id="content-article">
<p><a href="/tourney/pairings/${tourneyShort}"><b> ← back to the game list</b></a></p>
<br/>
<p>Game control: ${proposedTime}</p>
<table border="1" cellpadding="2" cellspacing="0">
<tr><th colspan="9">Possibly good times (server time)</th><th colspan="8" bgcolor="orange">Often bad <a href="/wiki/Generally_Bad_Times">times</a> (yellow)</th><th colspan="8" bgcolor="red">Always bad times (red)</th></tr>
${badTimes}
</table>
<br/>
<br/>
${htmlText}


<fieldset>

<form name="form" method="post" name="form" action="">



<p>
<textarea id="topicContents" name="contents" rows="25" cols="10" accesskey=","></textarea>
</p>
<p>

<div align="center"><table><tbody><tr><td>MM</td><td>DD</td><td>HH</td><td>mm</td></tr><tr><td><select name="month" cols="3"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option></select></td><td><select name="day"><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option><option value="24">24&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="26">26&nbsp;&nbsp;</option><option value="27">27&nbsp;&nbsp;</option><option value="28">28&nbsp;&nbsp;</option><option value="29">29&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="31">31&nbsp;&nbsp;</option></select></td><td><select name="hour"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option></select></td><td><select name="minute"><option value="0">00&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="35">35&nbsp;&nbsp;</option><option value="40">40&nbsp;&nbsp;</option><option value="45">45&nbsp;&nbsp;</option><option value="50">50&nbsp;&nbsp;</option><option value="55">55&nbsp;&nbsp;</option></select></td></tr></tbody></table><br/><!-- <input name="submit" value="Set Time" type="submit"> --></div>

<input type="submit" name="name" value="Submit text"  />
<input type="submit" name="name" value="Set clock" />
<input type="submit" name="name" value="Unset clock" />


</p>



</form>

</fieldset>


<div class="clear"></div>



	<br />
	</div>
</div>
<jsp:include page="footer.jsp"></jsp:include>
</div>
</div>


</body>
</html>
