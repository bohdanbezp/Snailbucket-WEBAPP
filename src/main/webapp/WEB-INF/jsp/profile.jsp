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
             <style>
                     #sortable, #sortable2 {  list-style-type: none; margin: 0; padding: 0; text-align: right; margin-right: 10px; margin-left: 10px; background: #eee; padding: 5px; width: 143px;}
                       #sortable li, #sortable2 li { margin: 5px; padding: 5px; font-size: 1.2em; width: 120px; }
                        #feedback { font-size: 1.4em; }
                          #selectable .ui-selecting { background: #FECA40; }
                          #selectable .ui-selected { background: #C00000; color: white; }
                          #hard_selectable .ui-selecting { background: #FECAA8; }
                          #hard_selectable .ui-selected { background: #FECA40; color: white; }
                          
                          #selectable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
                          #selectable li { margin: 3px; padding: 1px; float: left; width: 33px; height: 20px; font-size: 1em; text-align: center; }
                          #hard_selectable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
                          #hard_selectable li { margin: 3px; padding: 1px; float: left; width: 33px; height: 20px; font-size: 1em; text-align: center; }
                          
                          


                     </style>

          <script>
				function getSelectedTimes(thisItem) {
					var $selected = thisItem.children('.ui-selected');
					var text = $.map($selected, function(el){
                    return $(el).text()
                 }).join();
					return text;
				}
               function mapSelected(event,ui){
				 var text=getSelectedTimes($(this));
                 $('#bad_times').val(text)
               }
               function mapHardSelected(event,ui){
                   var text=getSelectedTimes($(this));
                   $('#hard_times').val(text)
                 }

               	$().ready(function() {
               	$("#buttonClear").click(function() {
               					//assume that button is into a parent item which wraps whole bad times selection 
                                 $(this).parent().find('.ui-selected').removeClass('ui-selected')
                                 $('#bad_times').val("");
                             });
				$("#buttonHardClear").click(function() {
								//assume that button is into a parent item which wraps whole hard times selection 
                                 $(this).parent().find('.ui-selected').removeClass('ui-selected')
                                 $('#hard_times').val("");
                             });
               	    $( "#selectable" ).selectable({
               	        filter: "li" ,
                             unselected:mapSelected,
                             selected:mapSelected
               	    });
               	 $( "#hard_selectable" ).selectable({
            	        filter: "li" ,
                          unselected:mapHardSelected,
                          selected:mapHardSelected
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

</div>
<div id="wiki-content">


<jsp:include page="reg.jsp"></jsp:include>

<div class="clear"></div>


<div id="tab-menu">





</div>
<div class="clear"></div>

	<div id="contents" >
	<h1 id="contents-header">Profile of ${username} <img src="/static/images/flags/${country}.png" alt="${country}"/></h1>
    <h3><noscript><font color="red">Please enable Javascript in order to have the best user experience with the website.</font></noscript></h3>


<div id="content-article">
 <h2>Change password</h2>
 <br/>
<form name="input" action="" method="post">
Password: <br/><input type="password" name="password"><input type="hidden" name="key" value="${key}"/><br/><br/>
Repeat password: <br/><input type="password" name="newpassword"><br/>
<br/><input name="submit" type="submit" value="Set new password"/></form>
<br/>

<form name="input" action="/profile/time" method="post">
<fieldset>
<h2>Change country</h2>
<label for="Country">Your country:</label>
                                        <select id="Country" name="country">
                                            <%
                                                String countryValue = (String) pageContext.findAttribute("country");
                                                    if (countryValue == null) {
                                                        countryValue = "--";
                                                    } else {
                                                        countryValue = countryValue.toLowerCase();
                                                    }
                                                    String currentCountry = countryValue;
                                            %>
                                            <option value="--" <%= "--".equals(currentCountry) ? "selected" : "" %>>Undefined</option>
                                            <option value="ca" <%= "ca".equals(currentCountry) ? "selected" : "" %>>Canada</option>
                                            <option value="us" <%= "us".equals(currentCountry) ? "selected" : "" %>>United States of America</option>
                                            <option value="gb" <%= "gb".equals(currentCountry) ? "selected" : "" %>>United Kingdom (Great Britain)</option>
                                            <option value="au" <%= "au".equals(currentCountry) ? "selected" : "" %>>Australia</option>
                                            <option value="jp" <%= "jp".equals(currentCountry) ? "selected" : "" %>>Japan</option>
                                            <option value="af" <%= "af".equals(currentCountry) ? "selected" : "" %>>Afghanistan</option>
                                            <option value="ax" <%= "ax".equals(currentCountry) ? "selected" : "" %>>Aland Island</option>
                                            <option value="al" <%= "al".equals(currentCountry) ? "selected" : "" %>>Albania</option>
                                            <option value="dz" <%= "dz".equals(currentCountry) ? "selected" : "" %>>Algeria</option>
                                            <option value="as" <%= "as".equals(currentCountry) ? "selected" : "" %>>American Samoa</option>
                                            <option value="ad" <%= "ad".equals(currentCountry) ? "selected" : "" %>>Andorra</option>
                                            <option value="ao" <%= "ao".equals(currentCountry) ? "selected" : "" %>>Angola</option>
                                            <option value="ai" <%= "ai".equals(currentCountry) ? "selected" : "" %>>Anguilla</option>
                                            <option value="aq" <%= "aq".equals(currentCountry) ? "selected" : "" %>>Antarctica</option>
                                            <option value="ag" <%= "ag".equals(currentCountry) ? "selected" : "" %>>Antigua & Barbuda</option>
                                            <option value="ar" <%= "ar".equals(currentCountry) ? "selected" : "" %>>Argentina</option>
                                            <option value="am" <%= "am".equals(currentCountry) ? "selected" : "" %>>Armenia</option>
                                            <option value="aw" <%= "aw".equals(currentCountry) ? "selected" : "" %>>Aruba</option>
                                            <option value="at" <%= "at".equals(currentCountry) ? "selected" : "" %>>Austria</option>
                                            <option value="az" <%= "az".equals(currentCountry) ? "selected" : "" %>>Azerbaijan</option>
                                            <option value="bs" <%= "bs".equals(currentCountry) ? "selected" : "" %>>Bahama</option>
                                            <option value="bh" <%= "bh".equals(currentCountry) ? "selected" : "" %>>Bahrain</option>
                                            <option value="bd" <%= "bd".equals(currentCountry) ? "selected" : "" %>>Bangladesh</option>
                                            <option value="bb" <%= "bb".equals(currentCountry) ? "selected" : "" %>>Barbados</option>
                                            <option value="by" <%= "by".equals(currentCountry) ? "selected" : "" %>>Belarus</option>
                                            <option value="be" <%= "be".equals(currentCountry) ? "selected" : "" %>>Belgium</option>
                                            <option value="bz" <%= "bz".equals(currentCountry) ? "selected" : "" %>>Belize</option>
                                            <option value="bj" <%= "bj".equals(currentCountry) ? "selected" : "" %>>Benin</option>
                                            <option value="bm" <%= "bm".equals(currentCountry) ? "selected" : "" %>>Bermuda</option>
                                            <option value="bt" <%= "bt".equals(currentCountry) ? "selected" : "" %>>Bhutan</option>
                                            <option value="bo" <%= "bo".equals(currentCountry) ? "selected" : "" %>>Bolivia</option>
                                            <option value="ba" <%= "ba".equals(currentCountry) ? "selected" : "" %>>Bosnia and Herzegovina</option>
                                            <option value="bw" <%= "bw".equals(currentCountry) ? "selected" : "" %>>Botswana</option>
                                            <option value="bv" <%= "bv".equals(currentCountry) ? "selected" : "" %>>Bouvet Island</option>
                                            <option value="br" <%= "br".equals(currentCountry) ? "selected" : "" %>>Brazil</option>
                                            <option value="io" <%= "io".equals(currentCountry) ? "selected" : "" %>>British Indian Ocean Territory</option>
                                            <option value="vg" <%= "vg".equals(currentCountry) ? "selected" : "" %>>British Virgin Islands</option>
                                            <option value="bn" <%= "bn".equals(currentCountry) ? "selected" : "" %>>Brunei Darussalam</option>
                                            <option value="bg" <%= "bg".equals(currentCountry) ? "selected" : "" %>>Bulgaria</option>
                                            <option value="bf" <%= "bf".equals(currentCountry) ? "selected" : "" %>>Burkina Faso</option>
                                            <option value="bi" <%= "bi".equals(currentCountry) ? "selected" : "" %>>Burundi</option>
                                            <option value="kh" <%= "kh".equals(currentCountry) ? "selected" : "" %>>Cambodia</option>
                                            <option value="cm" <%= "cm".equals(currentCountry) ? "selected" : "" %>>Cameroon</option>
                                            <option value="cv" <%= "cv".equals(currentCountry) ? "selected" : "" %>>Cape Verde</option>
                                            <option value="ky" <%= "ky".equals(currentCountry) ? "selected" : "" %>>Cayman Islands</option>
                                            <option value="cf" <%= "cf".equals(currentCountry) ? "selected" : "" %>>Central African Republic</option>
                                            <option value="td" <%= "td".equals(currentCountry) ? "selected" : "" %>>Chad</option>
                                            <option value="cl" <%= "cl".equals(currentCountry) ? "selected" : "" %>>Chile</option>
                                            <option value="cn" <%= "cn".equals(currentCountry) ? "selected" : "" %>>China</option>
                                            <option value="cx" <%= "cx".equals(currentCountry) ? "selected" : "" %>>Christmas Island</option>
                                            <option value="cc" <%= "cc".equals(currentCountry) ? "selected" : "" %>>Cocos (Keeling) Islands</option>
                                            <option value="co" <%= "co".equals(currentCountry) ? "selected" : "" %>>Colombia</option>
                                            <option value="km" <%= "km".equals(currentCountry) ? "selected" : "" %>>Comoros</option>
                                            <option value="cg" <%= "cg".equals(currentCountry) ? "selected" : "" %>>Congo</option>
                                            <option value="ck" <%= "ck".equals(currentCountry) ? "selected" : "" %>>Cook Islands</option>
                                            <option value="cr" <%= "cr".equals(currentCountry) ? "selected" : "" %>>Costa Rica</option>
                                            <option value="hr" <%= "hr".equals(currentCountry) ? "selected" : "" %>>Croatia</option>
                                            <option value="cu" <%= "cu".equals(currentCountry) ? "selected" : "" %>>Cuba</option>
                                            <option value="cy" <%= "cy".equals(currentCountry) ? "selected" : "" %>>Cyprus</option>
                                            <option value="cz" <%= "cz".equals(currentCountry) ? "selected" : "" %>>Czech Republic</option>
                                            <option value="ci" <%= "ci".equals(currentCountry) ? "selected" : "" %>>Côte D'ivoire (Ivory Coast)</option>
                                            <option value="dk" <%= "dk".equals(currentCountry) ? "selected" : "" %>>Denmark</option>
                                            <option value="dj" <%= "dj".equals(currentCountry) ? "selected" : "" %>>Djibouti</option>
                                            <option value="dm" <%= "dm".equals(currentCountry) ? "selected" : "" %>>Dominica</option>
                                            <option value="do" <%= "do".equals(currentCountry) ? "selected" : "" %>>Dominican Republic</option>
                                            <option value="tp" <%= "tp".equals(currentCountry) ? "selected" : "" %>>East Timor</option>
                                            <option value="ec" <%= "ec".equals(currentCountry) ? "selected" : "" %>>Ecuador</option>
                                            <option value="eg" <%= "eg".equals(currentCountry) ? "selected" : "" %>>Egypt</option>
                                            <option value="sv" <%= "sv".equals(currentCountry) ? "selected" : "" %>>El Salvador</option>
                                            <option value="gq" <%= "gq".equals(currentCountry) ? "selected" : "" %>>Equatorial Guinea</option>
                                            <option value="er" <%= "er".equals(currentCountry) ? "selected" : "" %>>Eritrea</option>
                                            <option value="ee" <%= "ee".equals(currentCountry) ? "selected" : "" %>>Estonia</option>
                                            <option value="et" <%= "et".equals(currentCountry) ? "selected" : "" %>>Ethiopia</option>
                                            <option value="fk" <%= "fk".equals(currentCountry) ? "selected" : "" %>>Falkland Islands (Malvinas)</option>
                                            <option value="fo" <%= "fo".equals(currentCountry) ? "selected" : "" %>>Faroe Islands</option>
                                            <option value="fj" <%= "fj".equals(currentCountry) ? "selected" : "" %>>Fiji</option>
                                            <option value="fi" <%= "fi".equals(currentCountry) ? "selected" : "" %>>Finland</option>
                                            <option value="fr" <%= "fr".equals(currentCountry) ? "selected" : "" %>>France</option>
                                            <option value="fx" <%= "fx".equals(currentCountry) ? "selected" : "" %>>France, Metropolitan</option>
                                            <option value="gf" <%= "gf".equals(currentCountry) ? "selected" : "" %>>French Guiana</option>
                                            <option value="pf" <%= "pf".equals(currentCountry) ? "selected" : "" %>>French Polynesia</option>
                                            <option value="tf" <%= "tf".equals(currentCountry) ? "selected" : "" %>>French Southern Territories</option>
                                            <option value="ga" <%= "ga".equals(currentCountry) ? "selected" : "" %>>Gabon</option>
                                            <option value="gm" <%= "gm".equals(currentCountry) ? "selected" : "" %>>Gambia</option>
                                            <option value="ge" <%= "ge".equals(currentCountry) ? "selected" : "" %>>Georgia</option>
                                            <option value="de" <%= "de".equals(currentCountry) ? "selected" : "" %>>Germany</option>
                                            <option value="gh" <%= "gh".equals(currentCountry) ? "selected" : "" %>>Ghana</option>
                                            <option value="gi" <%= "gi".equals(currentCountry) ? "selected" : "" %>>Gibraltar</option>
                                            <option value="gr" <%= "gr".equals(currentCountry) ? "selected" : "" %>>Greece</option>
                                            <option value="gl" <%= "gl".equals(currentCountry) ? "selected" : "" %>>Greenland</option>
                                            <option value="gd" <%= "gd".equals(currentCountry) ? "selected" : "" %>>Grenada</option>
                                            <option value="gp" <%= "gp".equals(currentCountry) ? "selected" : "" %>>Guadeloupe</option>
                                            <option value="gu" <%= "gu".equals(currentCountry) ? "selected" : "" %>>Guam</option>
                                            <option value="gt" <%= "gt".equals(currentCountry) ? "selected" : "" %>>Guatemala</option>
                                            <option value="gn" <%= "gn".equals(currentCountry) ? "selected" : "" %>>Guinea</option>
                                            <option value="gw" <%= "gw".equals(currentCountry) ? "selected" : "" %>>Guinea-Bissau</option>
                                            <option value="gy" <%= "gy".equals(currentCountry) ? "selected" : "" %>>Guyana</option>
                                            <option value="ht" <%= "ht".equals(currentCountry) ? "selected" : "" %>>Haiti</option>
                                            <option value="hm" <%= "hm".equals(currentCountry) ? "selected" : "" %>>Heard & McDonald Islands</option>
                                            <option value="hn" <%= "hn".equals(currentCountry) ? "selected" : "" %>>Honduras</option>
                                            <option value="hk" <%= "hk".equals(currentCountry) ? "selected" : "" %>>Hong Kong</option>
                                            <option value="hu" <%= "hu".equals(currentCountry) ? "selected" : "" %>>Hungary</option>
                                            <option value="is" <%= "is".equals(currentCountry) ? "selected" : "" %>>Iceland</option>
                                            <option value="in" <%= "in".equals(currentCountry) ? "selected" : "" %>>India</option>
                                            <option value="id" <%= "id".equals(currentCountry) ? "selected" : "" %>>Indonesia</option>
                                            <option value="iq" <%= "iq".equals(currentCountry) ? "selected" : "" %>>Iraq</option>
                                            <option value="ie" <%= "ie".equals(currentCountry) ? "selected" : "" %>>Ireland</option>
                                            <option value="ir" <%= "ir".equals(currentCountry) ? "selected" : "" %>>Islamic Republic of Iran</option>
                                            <option value="il" <%= "il".equals(currentCountry) ? "selected" : "" %>>Israel</option>
                                            <option value="it" <%= "it".equals(currentCountry) ? "selected" : "" %>>Italy</option>
                                            <option value="jm" <%= "jm".equals(currentCountry) ? "selected" : "" %>>Jamaica</option>
                                            <option value="jo" <%= "jo".equals(currentCountry) ? "selected" : "" %>>Jordan</option>
                                            <option value="kz" <%= "kz".equals(currentCountry) ? "selected" : "" %>>Kazakhstan</option>
                                            <option value="ke" <%= "ke".equals(currentCountry) ? "selected" : "" %>>Kenya</option>
                                            <option value="ki" <%= "ki".equals(currentCountry) ? "selected" : "" %>>Kiribati</option>
                                            <option value="kp" <%= "kp".equals(currentCountry) ? "selected" : "" %>>Korea, Democratic People's Republic of</option>
                                            <option value="kr" <%= "kr".equals(currentCountry) ? "selected" : "" %>>Korea, Republic of</option>
                                            <option value="kw" <%= "kw".equals(currentCountry) ? "selected" : "" %>>Kuwait</option>
                                            <option value="kg" <%= "kg".equals(currentCountry) ? "selected" : "" %>>Kyrgyzstan</option>
                                            <option value="la" <%= "la".equals(currentCountry) ? "selected" : "" %>>Lao People's Democratic Republic</option>
                                            <option value="lv" <%= "lv".equals(currentCountry) ? "selected" : "" %>>Latvia</option>
                                            <option value="lb" <%= "lb".equals(currentCountry) ? "selected" : "" %>>Lebanon</option>
                                            <option value="ls" <%= "ls".equals(currentCountry) ? "selected" : "" %>>Lesotho</option>
                                            <option value="lr" <%= "lr".equals(currentCountry) ? "selected" : "" %>>Liberia</option>
                                            <option value="ly" <%= "ly".equals(currentCountry) ? "selected" : "" %>>Libyan Arab Jamahiriya</option>
                                            <option value="li" <%= "li".equals(currentCountry) ? "selected" : "" %>>Liechtenstein</option>
                                            <option value="lt" <%= "lt".equals(currentCountry) ? "selected" : "" %>>Lithuania</option>
                                            <option value="lu" <%= "lu".equals(currentCountry) ? "selected" : "" %>>Luxembourg</option>
                                            <option value="mo" <%= "mo".equals(currentCountry) ? "selected" : "" %>>Macau</option>
                                            <option value="mg" <%= "mg".equals(currentCountry) ? "selected" : "" %>>Madagascar</option>
                                            <option value="mw" <%= "mw".equals(currentCountry) ? "selected" : "" %>>Malawi</option>
                                            <option value="my" <%= "my".equals(currentCountry) ? "selected" : "" %>>Malaysia</option>
                                            <option value="mv" <%= "mv".equals(currentCountry) ? "selected" : "" %>>Maldives</option>
                                            <option value="ml" <%= "ml".equals(currentCountry) ? "selected" : "" %>>Mali</option>
                                            <option value="mt" <%= "mt".equals(currentCountry) ? "selected" : "" %>>Malta</option>
                                            <option value="mh" <%= "mh".equals(currentCountry) ? "selected" : "" %>>Marshall Islands</option>
                                            <option value="mq" <%= "mq".equals(currentCountry) ? "selected" : "" %>>Martinique</option>
                                            <option value="mr" <%= "mr".equals(currentCountry) ? "selected" : "" %>>Mauritania</option>
                                            <option value="mu" <%= "mu".equals(currentCountry) ? "selected" : "" %>>Mauritius</option>
                                            <option value="yt" <%= "yt".equals(currentCountry) ? "selected" : "" %>>Mayotte</option>
                                            <option value="mx" <%= "mx".equals(currentCountry) ? "selected" : "" %>>Mexico</option>
                                            <option value="fm" <%= "fm".equals(currentCountry) ? "selected" : "" %>>Micronesia</option>
                                            <option value="md" <%= "md".equals(currentCountry) ? "selected" : "" %>>Moldova, Republic of</option>
                                            <option value="mc" <%= "mc".equals(currentCountry) ? "selected" : "" %>>Monaco</option>
                                            <option value="mn" <%= "mn".equals(currentCountry) ? "selected" : "" %>>Mongolia</option>
                                            <option value="ms" <%= "ms".equals(currentCountry) ? "selected" : "" %>>Montserrat</option>
                                            <option value="ma" <%= "ma".equals(currentCountry) ? "selected" : "" %>>Morocco</option>
                                            <option value="mz" <%= "mz".equals(currentCountry) ? "selected" : "" %>>Mozambique</option>
                                            <option value="mm" <%= "mm".equals(currentCountry) ? "selected" : "" %>>Myanmar</option>
                                            <option value="na" <%= "na".equals(currentCountry) ? "selected" : "" %>>Namibia</option>
                                            <option value="nr" <%= "nr".equals(currentCountry) ? "selected" : "" %>>Nauru</option>
                                            <option value="np" <%= "np".equals(currentCountry) ? "selected" : "" %>>Nepal</option>
                                            <option value="nl" <%= "nl".equals(currentCountry) ? "selected" : "" %>>Netherlands</option>
                                            <option value="an" <%= "an".equals(currentCountry) ? "selected" : "" %>>Netherlands Antilles</option>
                                            <option value="nc" <%= "nc".equals(currentCountry) ? "selected" : "" %>>New Caledonia</option>
                                            <option value="nz" <%= "nz".equals(currentCountry) ? "selected" : "" %>>New Zealand</option>
                                            <option value="ni" <%= "ni".equals(currentCountry) ? "selected" : "" %>>Nicaragua</option>
                                            <option value="ne" <%= "ne".equals(currentCountry) ? "selected" : "" %>>Niger</option>
                                            <option value="ng" <%= "ng".equals(currentCountry) ? "selected" : "" %>>Nigeria</option>
                                            <option value="nu" <%= "nu".equals(currentCountry) ? "selected" : "" %>>Niue</option>
                                            <option value="nf" <%= "nf".equals(currentCountry) ? "selected" : "" %>>Norfolk Island</option>
                                            <option value="mp" <%= "mp".equals(currentCountry) ? "selected" : "" %>>Northern Mariana Islands</option>
                                            <option value="no" <%= "no".equals(currentCountry) ? "selected" : "" %>>Norway</option>
                                            <option value="om" <%= "om".equals(currentCountry) ? "selected" : "" %>>Oman</option>
                                            <option value="pk" <%= "pk".equals(currentCountry) ? "selected" : "" %>>Pakistan</option>
                                            <option value="pw" <%= "pw".equals(currentCountry) ? "selected" : "" %>>Palau</option>
                                            <option value="pa" <%= "pa".equals(currentCountry) ? "selected" : "" %>>Panama</option>
                                            <option value="pg" <%= "pg".equals(currentCountry) ? "selected" : "" %>>Papua New Guinea</option>
                                            <option value="py" <%= "py".equals(currentCountry) ? "selected" : "" %>>Paraguay</option>
                                            <option value="pe" <%= "pe".equals(currentCountry) ? "selected" : "" %>>Peru</option>
                                            <option value="ph" <%= "ph".equals(currentCountry) ? "selected" : "" %>>Philippines</option>
                                            <option value="pn" <%= "pn".equals(currentCountry) ? "selected" : "" %>>Pitcairn</option>
                                            <option value="pl" <%= "pl".equals(currentCountry) ? "selected" : "" %>>Poland</option>
                                            <option value="pt" <%= "pt".equals(currentCountry) ? "selected" : "" %>>Portugal</option>
                                            <option value="pr" <%= "pr".equals(currentCountry) ? "selected" : "" %>>Puerto Rico</option>
                                            <option value="qa" <%= "qa".equals(currentCountry) ? "selected" : "" %>>Qatar</option>
                                            <option value="ro" <%= "ro".equals(currentCountry) ? "selected" : "" %>>Romania</option>
                                            <option value="ru" <%= "ru".equals(currentCountry) ? "selected" : "" %>>Russian Federation</option>
                                            <option value="rw" <%= "rw".equals(currentCountry) ? "selected" : "" %>>Rwanda</option>
                                            <option value="re" <%= "re".equals(currentCountry) ? "selected" : "" %>>Réunion</option>
                                            <option value="lc" <%= "lc".equals(currentCountry) ? "selected" : "" %>>Saint Lucia</option>
                                            <option value="ws" <%= "ws".equals(currentCountry) ? "selected" : "" %>>Samoa</option>
                                            <option value="sm" <%= "sm".equals(currentCountry) ? "selected" : "" %>>San Marino</option>
                                            <option value="st" <%= "st".equals(currentCountry) ? "selected" : "" %>>Sao Tome & Principe</option>
                                            <option value="sa" <%= "sa".equals(currentCountry) ? "selected" : "" %>>Saudi Arabia</option>
                                            <option value="sn" <%= "sn".equals(currentCountry) ? "selected" : "" %>>Senegal</option>
                                            <option value="rs" <%= "rs".equals(currentCountry) ? "selected" : "" %>>Serbia</option>
                                            <option value="sc" <%= "sc".equals(currentCountry) ? "selected" : "" %>>Seychelles</option>
                                            <option value="sl" <%= "sl".equals(currentCountry) ? "selected" : "" %>>Sierra Leone</option>
                                            <option value="sg" <%= "sg".equals(currentCountry) ? "selected" : "" %>>Singapore</option>
                                            <option value="sk" <%= "sk".equals(currentCountry) ? "selected" : "" %>>Slovakia</option>
                                            <option value="si" <%= "si".equals(currentCountry) ? "selected" : "" %>>Slovenia</option>
                                            <option value="sb" <%= "sb".equals(currentCountry) ? "selected" : "" %>>Solomon Islands</option>
                                            <option value="so" <%= "so".equals(currentCountry) ? "selected" : "" %>>Somalia</option>
                                            <option value="za" <%= "za".equals(currentCountry) ? "selected" : "" %>>South Africa</option>
                                            <option value="gs" <%= "gs".equals(currentCountry) ? "selected" : "" %>>South Georgia and the South Sandwich Islands</option>
                                            <option value="es" <%= "es".equals(currentCountry) ? "selected" : "" %>>Spain</option>
                                            <option value="lk" <%= "lk".equals(currentCountry) ? "selected" : "" %>>Sri Lanka</option>
                                            <option value="sh" <%= "sh".equals(currentCountry) ? "selected" : "" %>>St. Helena</option>
                                            <option value="kn" <%= "kn".equals(currentCountry) ? "selected" : "" %>>St. Kitts and Nevis</option>
                                            <option value="pm" <%= "pm".equals(currentCountry) ? "selected" : "" %>>St. Pierre & Miquelon</option>
                                            <option value="vc" <%= "vc".equals(currentCountry) ? "selected" : "" %>>St. Vincent & the Grenadines</option>
                                            <option value="sd" <%= "sd".equals(currentCountry) ? "selected" : "" %>>Sudan</option>
                                            <option value="sr" <%= "sr".equals(currentCountry) ? "selected" : "" %>>Suriname</option>
                                            <option value="sj" <%= "sj".equals(currentCountry) ? "selected" : "" %>>Svalbard & Jan Mayen Islands</option>
                                            <option value="sz" <%= "sz".equals(currentCountry) ? "selected" : "" %>>Swaziland</option>
                                            <option value="se" <%= "se".equals(currentCountry) ? "selected" : "" %>>Sweden</option>
                                            <option value="ch" <%= "ch".equals(currentCountry) ? "selected" : "" %>>Switzerland</option>
                                            <option value="sy" <%= "sy".equals(currentCountry) ? "selected" : "" %>>Syrian Arab Republic</option>
                                            <option value="tw" <%= "tw".equals(currentCountry) ? "selected" : "" %>>Taiwan, Province of China</option>
                                            <option value="tj" <%= "tj".equals(currentCountry) ? "selected" : "" %>>Tajikistan</option>
                                            <option value="tz" <%= "tz".equals(currentCountry) ? "selected" : "" %>>Tanzania, United Republic of</option>
                                            <option value="th" <%= "th".equals(currentCountry) ? "selected" : "" %>>Thailand</option>
                                            <option value="tg" <%= "tg".equals(currentCountry) ? "selected" : "" %>>Togo</option>
                                            <option value="tk" <%= "tk".equals(currentCountry) ? "selected" : "" %>>Tokelau</option>
                                            <option value="to" <%= "to".equals(currentCountry) ? "selected" : "" %>>Tonga</option>
                                            <option value="tt" <%= "tt".equals(currentCountry) ? "selected" : "" %>>Trinidad & Tobago</option>
                                            <option value="tn" <%= "tn".equals(currentCountry) ? "selected" : "" %>>Tunisia</option>
                                            <option value="tr" <%= "tr".equals(currentCountry) ? "selected" : "" %>>Turkey</option>
                                            <option value="tm" <%= "tm".equals(currentCountry) ? "selected" : "" %>>Turkmenistan</option>
                                            <option value="tc" <%= "tc".equals(currentCountry) ? "selected" : "" %>>Turks & Caicos Islands</option>
                                            <option value="tv" <%= "tv".equals(currentCountry) ? "selected" : "" %>>Tuvalu</option>
                                            <option value="ug" <%= "ug".equals(currentCountry) ? "selected" : "" %>>Uganda</option>
                                            <option value="ua" <%= "ua".equals(currentCountry) ? "selected" : "" %>>Ukraine</option>
                                            <option value="ae" <%= "ae".equals(currentCountry) ? "selected" : "" %>>United Arab Emirates</option>
                                            <option value="um" <%= "um".equals(currentCountry) ? "selected" : "" %>>United States Minor Outlying Islands</option>
                                            <option value="vi" <%= "vi".equals(currentCountry) ? "selected" : "" %>>United States Virgin Islands</option>
                                            <option value="uy" <%= "uy".equals(currentCountry) ? "selected" : "" %>>Uruguay</option>
                                            <option value="uz" <%= "uz".equals(currentCountry) ? "selected" : "" %>>Uzbekistan</option>
                                            <option value="vu" <%= "vu".equals(currentCountry) ? "selected" : "" %>>Vanuatu</option>
                                            <option value="va" <%= "va".equals(currentCountry) ? "selected" : "" %>>Vatican City State (Holy See)</option>
                                            <option value="ve" <%= "ve".equals(currentCountry) ? "selected" : "" %>>Venezuela</option>
                                            <option value="vn" <%= "vn".equals(currentCountry) ? "selected" : "" %>>Viet Nam</option>
                                            <option value="wf" <%= "wf".equals(currentCountry) ? "selected" : "" %>>Wallis & Futuna Islands</option>
                                            <option value="eh" <%= "eh".equals(currentCountry) ? "selected" : "" %>>Western Sahara</option>
                                            <option value="ye" <%= "ye".equals(currentCountry) ? "selected" : "" %>>Yemen</option>
                                            <option value="zr" <%= "zr".equals(currentCountry) ? "selected" : "" %>>Zaire</option>
                                            <option value="zm" <%= "zm".equals(currentCountry) ? "selected" : "" %>>Zambia</option>
                                            <option value="zw" <%= "zw".equals(currentCountry) ? "selected" : "" %>>Zimbabwe</option>
                                        </select>
                                        <input name="submit" type="submit" value="Submit"/>
<br/>
<br/>
<h2>Change time control preferences</h2>
<label for="sortable">Feel free to list <a href="/wiki/Time_Control_Preferences">time control preferences</a> (you can drag and drop elements):</label>
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
                                 <input name="submit" type="submit" value="Submit"/>
                                 <br/><br/>
                                 <h2>Change generally bad times</h2>
                                 <br/>
                                                        <p>Below, please mark times (*in server time*) <a href="/wiki/Generally_Bad_Times">that are bad for you</a> - this will help your opponent schedule the game with you.</p>

                                                           <p>The following times are <font color="#C00000">always bad</font> (all 7 days of the week, e.g. sleeping times):</p>
								<div id="generally_bad_times">
                                <ol id="selectable">
                                                                                                                      ${selectable}</ol>
                                                                                                                    <input type="hidden" id="bad_times" name="bad_times" value="${bad_times}" />
                                                                                                                    <br/><p>(hold LEFT CTRL/STRG key and move mouse to select multiple ranges)</p>
								
                                                                                                                                                                                            <div id="buttonClear">Click here to clear</div><br/>
								</div>
								
                                                           <p>The following times are <font color="#FECA40">often bad</font> (some days of the week, e.g. school/work times):</p>
								<div id="generally_hard_times">
                                <ol id="hard_selectable">
                                                                                                                      ${hard_selectable}</ol>
                                                                                                                    <input type="hidden" id="hard_times" name="hard_times" value="${hard_times}" />
                                                                                                                    <br/><p>(hold LEFT CTRL/STRG key and move mouse to select multiple ranges)</p>
								
                                                                                                                                                                                            <div id="buttonHardClear">Click here to clear</div><br/>
								</div>								
								

         <input name="tcpref" type="submit" value="Submit"/>
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
