<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - ${title}</title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jquery-ui.css" type="text/css" rel="stylesheet" />
    <script type="text/javascript" src="/static/jquery.js"></script>
    <script type="text/javascript" src="/static/jquery.validate.js"></script>
    <script type="text/javascript" src="/static/jquery-ui.js"></script>
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />

  <style type="text/css">
            body { font-family:Lucida Sans, Arial, Helvetica, Sans-Serif; font-size:13px; margin:20px;}
            #main { margin: 0px auto; border:solid 1px #b2b3b5; -moz-border-radius:10px; padding:20px; background-color:#f6f6f6;}
            #header { text-align:center; border-bottom:solid 1px #b2b3b5; margin: 0 0 20px 0; }
            fieldset { border:none; width:520px;}
            legend { font-size:18px; margin:0px; padding:10px 0px; color:#b0232a; font-weight:bold;}
            label { display:block; margin:15px 0 5px;}
            input[type=text], input[type=password] { width:300px; padding:5px; border:solid 1px #000;}
            .prev, .next { background-color:#b0232a; padding:5px 10px; color:#fff; text-decoration:none;}
            .prev:hover, .next:hover { background-color:#000; text-decoration:none;}
            .prev { float:left;}
            .next { float:right;}
            #steps { list-style:none; width:100%; overflow:hidden; margin:0px; padding:0px;}
            #steps li {font-size:24px; float:left; padding:10px; color:#b0b1b3;}
            #steps li span {font-size:11px; display:block;}
            #steps li.current { color:#000;}
            #makeWizard { background-color:#b0232a; color:#fff; padding:5px 10px; text-decoration:none; font-size:18px;}
            #makeWizard:hover { background-color:#000;}
            .radioLeft
            {
               float: left;
            }

            .textBlock
            {
                float: left;
                width: 80%; //Adjust this value to fit
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
             <style>
                     #sortable, #sortable2 {  list-style-type: none; margin: 0; padding: 0; text-align: right; margin-right: 10px; margin-left: 10px; background: #eee; padding: 5px; width: 143px;}
                       #sortable li, #sortable2 li { margin: 5px; padding: 5px; font-size: 1.2em; width: 120px; }
                        #feedback { font-size: 1.4em; }
                          #selectable .ui-selecting { background: #FECA40; }
                          #selectable .ui-selected { background: #C00000; color: white; }
                          #selectable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
                          #selectable li { margin: 3px; padding: 1px; float: left; width: 33px; height: 20px; font-size: 1em; text-align: center; }


                     </style>

          <script>
               function mapSelected(event,ui){
                 var $selected = $(this).children('.ui-selected');
                 var text = $.map($selected, function(el){
                    return $(el).text()
                 }).join();
                 $('#bad_times').val(text)
               }

               	$().ready(function() {
               	$("#buttonClear").click(function() {
                                 $(".ui-selected").removeClass("ui-selected");
                                 $('#bad_times').val("");
                             });
               	    $( "#selectable" ).selectable({
               	        filter: "li" ,
                             unselected:mapSelected,
                             selected:mapSelected
               	    });
               	    $( "#sortable, #sortable2" ).sortable({
               	     connectWith: ".connectedSortable",
                      beforeStop: function (event, ui) {
                              if ($("#sortable2").find('#45_45').length) {
                                  // about to drop item into #list3, so cancel the sort
                                  return false;
                              }
                          },
                      update: function(event, ui) {
                                     var order = $("#sortable").sortable("toArray");
                                     $('#time_order').val(order.join(","));
                                 }
               	    });

                     $( "#sortable" ).disableSelection();
                     $( "#sortable2" ).disableSelection();

                                         		});
                                         		</script>
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
	<h1 id="contents-header">Profile of ${username}</h1>



<div id="content-article">
 <h2>Change password</h2>
 <br/>
<form name="input" action="" method="post">
Password: <br/><input type="password" name="password"><input type="hidden" name="key" value="${key}"/><br/><br/>
Repeat password: <br/><input type="password" name="newpassword"><br/>
<br/><input name="submit" type="submit" value="Set new password"/></form>
<br/><br/>

<h2>Change time control preferences</h2>
<form name="input" action="/profile/time" method="post">
<fieldset>
<label for="sortable">My preferences are (you can drag and drop elements):</label>
<input type="hidden" id="time_order" name="time_order" value="${time_order}" />
                          <table border="1" cellpadding="10">
                          <tr><td><center>Preferred time controls,<br>
                                          in this order:</center></td><td><center>Excluded time controls:</center></td></tr>
                          <tr><td><ul id="sortable" class="connectedSortable">
                             ${sortable1}
                          </ul>
                          </td><td>
                          <ul id="sortable2" class="connectedSortable">
                                                    ${sortable2}
                                                    </ul>
                                </td></tr> </table> <br/>
                                 <br/>
                                                        <hr style="height:1px;border:none;color:#333;background-color:#333;"/>
                                                        <p>Are there times that are GENERALLY BAD for you, on weekdays AND on the weekend, for example, because they are your sleeping times? Then you may mark them red below - this will help your opponent schedule the game with you.</p>

                                                           <p>The following times are <font color="#C00000">generally bad for me</font> (FICS server time; hold <b>LEFT CTRL/STRG</b> key and move mouse to select multiple ranges):</p>

                                <ol id="selectable">
                                                                                                                      ${selectable}</ol>
                                                                                                                    <input type="hidden" id="bad_times" name="bad_times" value="${bad_times}" />
                                                                                                                    <br/>
                                                                                                                                                                                            <div id="buttonClear">Click here to clear</div><br/>

         <input name="tcpref" type="submit" value="Update"/>
</fieldset>
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