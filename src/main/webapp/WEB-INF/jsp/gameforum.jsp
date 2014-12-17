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
<p><a href="/tourney/pairings/bucket1:R${round}"><b> ← back to the game list</b></a></p>
<p>Local time:</p>
<object type="application/x-shockwave-flash" height="57" width="78" data="http://www.usflashmap.com/component/flash_clock/flash_clock.swf">
<param name="allowScriptAccess" value="sameDomain" />
<param name="movie" value="http://www.usflashmap.com/component/flash_clock/flash_clock.swf" />
<param name="base" value="http://www.usflashmap.com/component/flash_clock/" />
<param name="flashvars" value="
      &gmt=local&
      &dl_start=undefined&
      &dl_end=undefined&
      &bodycolor=0x0066ff&
      &scolor=0xff0000&
      &facecolor=0xcccccc&
      &bodyflare=true&
      &faceflare=false&
      &numeralcolor=0xffffff&
      &labelarray=Local Time&
      &labelTextField=0:45:160:25&
      &dataTextField=0:160:160:25&
      &timeTextField=0:95:160:25&
      &showlabelTextField=false&
      &showdataTextField=false&
      &showtimeTextField=true&
      &labelTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &dataTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &timeTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &clock_type=digital&
      &clock_sound=false&
      &imagearray=&
      &time_style=24&
" />
<param name="quality" value="high" />
<param name="scale" value="noscale" />
<param name="salign" value="lt" />
<param name="bgcolor" value="#ffffff" />
</object>

<p>GMT:</p>
<object type="application/x-shockwave-flash" height="57" width="78" data="http://www.usflashmap.com/component/flash_clock/flash_clock.swf">
<param name="allowScriptAccess" value="sameDomain" />
<param name="movie" value="http://www.usflashmap.com/component/flash_clock/flash_clock.swf" />
<param name="base" value="http://www.usflashmap.com/component/flash_clock/" />
<param name="flashvars" value="
      &gmt=0&
      &dl_start=3|2SU|2&
      &dl_end=11|1SU|2&
      &bodycolor=0x0066ff&
      &scolor=0xff0000&
      &facecolor=0xcccccc&
      &bodyflare=true&
      &faceflare=false&
      &numeralcolor=0xffffff&
      &labelarray=Local Time&
      &labelTextField=0:45:160:25&
      &dataTextField=0:160:160:25&
      &timeTextField=0:95:160:25&
      &showlabelTextField=false&
      &showdataTextField=false&
      &showtimeTextField=true&
      &labelTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &dataTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &timeTextFieldStyle=<p align='center'><font face='Arial' color='#000000' size='18'><b>[]</b></font></p>&
      &clock_type=digital&
      &clock_sound=false&
      &imagearray=&
      &time_style=24&
" />
<param name="quality" value="high" />
<param name="scale" value="noscale" />
<param name="salign" value="lt" />
<param name="bgcolor" value="#ffffff" />
</object>
<br/>
<table border="1" cellpadding="2" cellspacing="0">
<tr><th colspan="12">Possibly good times (GMT)</th><th colspan="13" bgcolor="red"><a href="/wiki/Generally_Bad_Times">Generally</a> bad times (GMT)</th></tr>
${badTimes}
</table>
<br/>
<p style="border: 1px solid #005cb9; background-color: #f1f5f9; overflow: auto; display:inline;padding: 2px;"><b><a href="/wiki/Matching_time_controls_algorithm">Time control of the game</a>: ${proposedTime}</b></p>
<br/>
<br/>
${htmlText}


<fieldset>

<form name="form" method="post" name="editform" action="">



<p>
<textarea id="topicContents" name="contents" rows="25" cols="10" accesskey=","></textarea>
</p>
<p>

<div align="center"><table><tbody><tr><td>MM</td><td>DD</td><td>HH</td><td>mm</td></tr><tr><td><select name="month" cols="3"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option></select></td><td><select name="day"><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option><option value="24">24&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="26">26&nbsp;&nbsp;</option><option value="27">27&nbsp;&nbsp;</option><option value="28">28&nbsp;&nbsp;</option><option value="29">29&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="31">31&nbsp;&nbsp;</option></select></td><td><select name="hour"><option value="0">00&nbsp;&nbsp;</option><option value="1">01&nbsp;&nbsp;</option><option value="2">02&nbsp;&nbsp;</option><option value="3">03&nbsp;&nbsp;</option><option value="4">04&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="6">06&nbsp;&nbsp;</option><option value="7">07&nbsp;&nbsp;</option><option value="8">08&nbsp;&nbsp;</option><option value="9">09&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="11">11&nbsp;&nbsp;</option><option value="12">12&nbsp;&nbsp;</option><option value="13">13&nbsp;&nbsp;</option><option value="14">14&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="16">16&nbsp;&nbsp;</option><option value="17">17&nbsp;&nbsp;</option><option value="18">18&nbsp;&nbsp;</option><option value="19">19&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="21">21&nbsp;&nbsp;</option><option value="22">22&nbsp;&nbsp;</option><option value="23">23&nbsp;&nbsp;</option></select></td><td><select name="minute"><option value="0">00&nbsp;&nbsp;</option><option value="5">05&nbsp;&nbsp;</option><option value="10">10&nbsp;&nbsp;</option><option value="15">15&nbsp;&nbsp;</option><option value="20">20&nbsp;&nbsp;</option><option value="25">25&nbsp;&nbsp;</option><option value="30">30&nbsp;&nbsp;</option><option value="35">35&nbsp;&nbsp;</option><option value="40">40&nbsp;&nbsp;</option><option value="45">45&nbsp;&nbsp;</option><option value="50">50&nbsp;&nbsp;</option><option value="55">55&nbsp;&nbsp;</option></select></td></tr></tbody></table><br/><!-- <input name="submit" value="Set Time" type="submit"> --></div>

<input type="submit" name="save" value="Confirm"  accesskey="s" />


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