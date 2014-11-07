<%@page pageEncoding="UTF-8" %>

<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<head>
  <title>Snail Bucket - Registration form</title>
  <meta name="robots" content="noindex,nofollow" />
  <link href="/static/jspwiki.css" type="text/css" rel="stylesheet" />
  <link href="/static/jquery-ui.css" type="text/css" rel="stylesheet" />
  <script type="text/javascript" src="/static/jquery.js"></script>
  <script type="text/javascript" src="/static/jquery.validate.js"></script>
  <script type="text/javascript" src="/static/jquery-ui.js"></script>
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
        #sortable, #sortable2 {  list-style-type: none; margin: 0; padding: 0; text-align: right; margin-right: 10px; margin-left: 10px; background: #eee; padding: 5px; width: 143px;}
          #sortable li, #sortable2 li { margin: 5px; padding: 5px; font-size: 1.2em; width: 120px; }
           #feedback { font-size: 1.4em; }
             #selectable .ui-selecting { background: #FECA40; }
             #selectable .ui-selected { background: #C00000; color: white; }
             #selectable { list-style-type: none; margin: 0; padding: 0; width: 100%; }
             #selectable li { margin: 3px; padding: 1px; float: left; width: 33px; height: 20px; font-size: 1em; text-align: center; }


        </style>
      <style>
      	#SignupForm label.error {
      		color:#FF0000;
      	}
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
      	    $("#def_time").change(function() {
                if(this.checked) {
                  $("#var1").slideUp("slow");
                }
                else {
                  $("#var1").slideDown("slow");
                }
            });

            $("#op_dec").change(function() {
                            if(this.checked) {
                              $("#var2").slideDown("slow");
                            }
                            else {
                              $("#var2").slideUp("slow");
                            }
                        });

             $("#var1").hide();
      	    $("#SignupForm").validate({
                                			rules: {
                                				Username: {
                                					required: true,
                                					minlength: 2
                                				},
                                				Password: {
                                					required: true,
                                					minlength: 5
                                				},
                                				Confirm_password: {
                                					required: true,
                                					minlength: 5,
                                					equalTo: "#Password"
                                				},
                                				Email: {
                                					required: true,
                                					email: true
                                				}
                                			},
                                			messages: {
                                				Username: {
                                					required: "Please enter a username",
                                					minlength: "Your username must consist of at least 2 characters"
                                				},
                                				Password: {
                                					required: "Please provide a password",
                                					minlength: "Your password must be at least 5 characters long"
                                				},
                                				Confirm_password: {
                                					required: "Please provide a password",
                                					minlength: "Your password must be at least 5 characters long",
                                					equalTo: "Please enter the same password as above"
                                				},
                                				Email: "Please enter a valid email address"
                                			}
                                		});
                                		});
                                		</script>
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
</head>                                         <jsp:include page="tracking.jsp"></jsp:include>
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
	<h1 id="contents-header">Registration form</h1>



<div id="content-article">
                  <h3><noscript><font color="red">Please enable Javascript in order to have the best user experience with the website.</font></noscript></h3>
<form id="SignupForm" action="/wiki/Special:Register" method="post">
        <fieldset>
            <legend>Account information</legend>
            <label for="Username">FICS handle:</label>
            <input id="Username" name="Username" type="text" />
            <label for="Email">Email:</label>
            <input id="Email" name="Email" type="text" />
            <label for="Password">Password:</label>
            <input id="Password" name="Password" type="password" />
            <label for="Confirm_password">Confirm password:</label>
            <input id="Confirm_password" name="Confirm_password" type="password" />
            <label for="Country">Your country (optional):</label>
             <select id="Country" name="Country">
                            <option value="--">Undefined</option>
            			    <option value="CA">Canada</option>
                            <option value="US">United States of America</option>
                            <option value="GB">United Kingdom (Great Britain)</option>
                            <option value="AU">Australia</option>
                            <option value="JP">Japan</option>
                            <option value="AF">Afghanistan</option>
                            <option value="AX">Aland Island</option>
                            <option value="AL">Albania</option>
                            <option value="DZ">Algeria</option>
                            <option value="AS">American Samoa</option>
                            <option value="AD">Andorra</option>
                            <option value="AO">Angola</option>
                            <option value="AI">Anguilla</option>
                            <option value="AQ">Antarctica</option>
                            <option value="AG">Antigua & Barbuda</option>
                            <option value="AR">Argentina</option>
                            <option value="AM">Armenia</option>
                            <option value="AW">Aruba</option>
                            <option value="AT">Austria</option>
                            <option value="AZ">Azerbaijan</option>
                            <option value="BS">Bahama</option>
                            <option value="BH">Bahrain</option>
                            <option value="BD">Bangladesh</option>
                            <option value="BB">Barbados</option>
                            <option value="BY">Belarus</option>
                            <option value="BE">Belgium</option>
                            <option value="BZ">Belize</option>
                            <option value="BJ">Benin</option>
                            <option value="BM">Bermuda</option>
                            <option value="BT">Bhutan</option>
                            <option value="BO">Bolivia</option>
                            <option value="BA">Bosnia and Herzegovina</option>
                            <option value="BW">Botswana</option>
                            <option value="BV">Bouvet Island</option>
                            <option value="BR">Brazil</option>
                            <option value="IO">British Indian Ocean Territory</option>
                            <option value="VG">British Virgin Islands</option>
                            <option value="BN">Brunei Darussalam</option>
                            <option value="BG">Bulgaria</option>
                            <option value="BF">Burkina Faso</option>
                            <option value="BI">Burundi</option>
                            <option value="KH">Cambodia</option>
                            <option value="CM">Cameroon</option>
                            <option value="CV">Cape Verde</option>
                            <option value="KY">Cayman Islands</option>
                            <option value="CF">Central African Republic</option>
                            <option value="TD">Chad</option>
                            <option value="CL">Chile</option>
                            <option value="CN">China</option>
                            <option value="CX">Christmas Island</option>
                            <option value="CC">Cocos (Keeling) Islands</option>
                            <option value="CO">Colombia</option>
                            <option value="KM">Comoros</option>
                            <option value="CG">Congo</option>
                            <option value="CK">Cook Iislands</option>
                            <option value="CR">Costa Rica</option>
                            <option value="HR">Croatia</option>
                            <option value="CU">Cuba</option>
                            <option value="CY">Cyprus</option>
                            <option value="CZ">Czech Republic</option>
                            <option value="CI">Côte D'ivoire (Ivory Coast)</option>
                            <option value="DK">Denmark</option>
                            <option value="DJ">Djibouti</option>
                            <option value="DM">Dominica</option>
                            <option value="DO">Dominican Republic</option>
                            <option value="TP">East Timor</option>
                            <option value="EC">Ecuador</option>
                            <option value="EG">Egypt</option>
                            <option value="SV">El Salvador</option>
                            <option value="GQ">Equatorial Guinea</option>
                            <option value="ER">Eritrea</option>
                            <option value="EE">Estonia</option>
                            <option value="ET">Ethiopia</option>
                            <option value="FK">Falkland Islands (Malvinas)</option>
                            <option value="FO">Faroe Islands</option>
                            <option value="FJ">Fiji</option>
                            <option value="FI">Finland</option>
                            <option value="FR">France</option>
                            <option value="FX">France, Metropolitan</option>
                            <option value="GF">French Guiana</option>
                            <option value="PF">French Polynesia</option>
                            <option value="TF">French Southern Territories</option>
                            <option value="GA">Gabon</option>
                            <option value="GM">Gambia</option>
                            <option value="GE">Georgia</option>
                            <option value="DE">Germany</option>
                            <option value="GH">Ghana</option>
                            <option value="GI">Gibraltar</option>
                            <option value="GR">Greece</option>
                            <option value="GL">Greenland</option>
                            <option value="GD">Grenada</option>
                            <option value="GP">Guadeloupe</option>
                            <option value="GU">Guam</option>
                            <option value="GT">Guatemala</option>
                            <option value="GN">Guinea</option>
                            <option value="GW">Guinea-Bissau</option>
                            <option value="GY">Guyana</option>
                            <option value="HT">Haiti</option>
                            <option value="HM">Heard & McDonald Islands</option>
                            <option value="HN">Honduras</option>
                            <option value="HK">Hong Kong</option>
                            <option value="HU">Hungary</option>
                            <option value="IS">Iceland</option>
                            <option value="IN">India</option>
                            <option value="ID">Indonesia</option>
                            <option value="IQ">Iraq</option>
                            <option value="IE">Ireland</option>
                            <option value="IR">Islamic Republic of Iran</option>
                            <option value="IL">Israel</option>
                            <option value="IT">Italy</option>
                            <option value="JM">Jamaica</option>
                            <option value="JO">Jordan</option>
                            <option value="KZ">Kazakhstan</option>
                            <option value="KE">Kenya</option>
                            <option value="KI">Kiribati</option>
                            <option value="KP">Korea, Democratic People's Republic of</option>
                            <option value="KR">Korea, Republic of</option>
                            <option value="KW">Kuwait</option>
                            <option value="KG">Kyrgyzstan</option>
                            <option value="LA">Lao People's Democratic Republic</option>
                            <option value="LV">Latvia</option>
                            <option value="LB">Lebanon</option>
                            <option value="LS">Lesotho</option>
                            <option value="LR">Liberia</option>
                            <option value="LY">Libyan Arab Jamahiriya</option>
                            <option value="LI">Liechtenstein</option>
                            <option value="LT">Lithuania</option>
                            <option value="LU">Luxembourg</option>
                            <option value="MO">Macau</option>
                            <option value="MG">Madagascar</option>
                            <option value="MW">Malawi</option>
                            <option value="MY">Malaysia</option>
                            <option value="MV">Maldives</option>
                            <option value="ML">Mali</option>
                            <option value="MT">Malta</option>
                            <option value="MH">Marshall Islands</option>
                            <option value="MQ">Martinique</option>
                            <option value="MR">Mauritania</option>
                            <option value="MU">Mauritius</option>
                            <option value="YT">Mayotte</option>
                            <option value="MX">Mexico</option>
                            <option value="FM">Micronesia</option>
                            <option value="MD">Moldova, Republic of</option>
                            <option value="MC">Monaco</option>
                            <option value="MN">Mongolia</option>
                            <option value="MS">Monserrat</option>
                            <option value="MA">Morocco</option>
                            <option value="MZ">Mozambique</option>
                            <option value="MM">Myanmar</option>
                            <option value="NA">Namibia</option>
                            <option value="NR">Nauru</option>
                            <option value="NP">Nepal</option>
                            <option value="NL">Netherlands</option>
                            <option value="AN">Netherlands Antilles</option>
                            <option value="NC">New Caledonia</option>
                            <option value="NZ">New Zealand</option>
                            <option value="NI">Nicaragua</option>
                            <option value="NE">Niger</option>
                            <option value="NG">Nigeria</option>
                            <option value="NU">Niue</option>
                            <option value="NF">Norfolk Island</option>
                            <option value="MP">Northern Mariana Islands</option>
                            <option value="NO">Norway</option>
                            <option value="OM">Oman</option>
                            <option value="PK">Pakistan</option>
                            <option value="PW">Palau</option>
                            <option value="PA">Panama</option>
                            <option value="PG">Papua New Guinea</option>
                            <option value="PY">Paraguay</option>
                            <option value="PE">Peru</option>
                            <option value="PH">Philippines</option>
                            <option value="PN">Pitcairn</option>
                            <option value="PL">Poland</option>
                            <option value="PT">Portugal</option>
                            <option value="PR">Puerto Rico</option>
                            <option value="QA">Qatar</option>
                            <option value="RO">Romania</option>
                            <option value="RU">Russian Federation</option>
                            <option value="RW">Rwanda</option>
                            <option value="RE">Réunion</option>
                            <option value="LC">Saint Lucia</option>
                            <option value="WS">Samoa</option>
                            <option value="SM">San Marino</option>
                            <option value="ST">Sao Tome & Principe</option>
                            <option value="SA">Saudi Arabia</option>
                            <option value="SN">Senegal</option>
                            <option value="RS">Serbia</option>
                            <option value="SC">Seychelles</option>
                            <option value="SL">Sierra Leone</option>
                            <option value="SG">Singapore</option>
                            <option value="SK">Slovakia</option>
                            <option value="SI">Slovenia</option>
                            <option value="SB">Solomon Islands</option>
                            <option value="SO">Somalia</option>
                            <option value="ZA">South Africa</option>
                            <option value="GS">South Georgia and the South Sandwich Islands</option>
                            <option value="ES">Spain</option>
                            <option value="LK">Sri Lanka</option>
                            <option value="SH">St. Helena</option>
                            <option value="KN">St. Kitts and Nevis</option>
                            <option value="PM">St. Pierre & Miquelon</option>
                            <option value="VC">St. Vincent & the Grenadines</option>
                            <option value="SD">Sudan</option>
                            <option value="SR">Suriname</option>
                            <option value="SJ">Svalbard & Jan Mayen Islands</option>
                            <option value="SZ">Swaziland</option>
                            <option value="SE">Sweden</option>
                            <option value="CH">Switzerland</option>
                            <option value="SY">Syrian Arab Republic</option>
                            <option value="TW">Taiwan, Province of China</option>
                            <option value="TJ">Tajikistan</option>
                            <option value="TZ">Tanzania, United Republic of</option>
                            <option value="TH">Thailand</option>
                            <option value="TG">Togo</option>
                            <option value="TK">Tokelau</option>
                            <option value="TO">Tonga</option>
                            <option value="TT">Trinidad & Tobago</option>
                            <option value="TN">Tunisia</option>
                            <option value="TR">Turkey</option>
                            <option value="TM">Turkmenistan</option>
                            <option value="TC">Turks & Caicos Islands</option>
                            <option value="TV">Tuvalu</option>
                            <option value="UG">Uganda</option>
                            <option value="UA">Ukraine</option>
                            <option value="AE">United Arab Emirates</option>
                            <option value="UM">United States Minor Outlying Islands</option>
                            <option value="VI">United States Virgin Islands</option>
                            <option value="UY">Uruguay</option>
                            <option value="UZ">Uzbekistan</option>
                            <option value="VU">Vanuatu</option>
                            <option value="VA">Vatican City State (Holy See)</option>
                            <option value="VE">Venezuela</option>
                            <option value="VN">Viet Nam</option>
                            <option value="WF">Wallis & Futuna Islands</option>
                            <option value="EH">Western Sahara</option>
                            <option value="YE">Yemen</option>
                            <option value="ZR">Zaire</option>
                            <option value="ZM">Zambia</option>
                            <option value="ZW">Zimbabwe</option>
                        </select>
                        <br/>
                        <br/>
                        <hr style="height:1px;border:none;color:#333;background-color:#333;"/>
                        <p>Are there times that are GENERALLY BAD for you, on weekdays AND on the weekend, for example, because they are your sleeping times? Then you may mark them red below - this will help your opponent schedule the game with you.</p>

                           <p>The following times are <font color="#C00000">generally bad for me</font> (FICS server time; hold <b>LEFT CTRL/STRG</b> key and move mouse to select multiple ranges):</p>

                                                                         <div id="var2">
                                                                              <ol id="selectable">
                                                                                       <li class="ui-widget-content">1</li>
                                                                                       <li class="ui-widget-content">2</li>
                                                                                       <li class="ui-widget-content">3</li>
                                                                                       <li class="ui-widget-content">4</li>
                                                                                       <li class="ui-widget-content">5</li>
                                                                                       <li class="ui-widget-content">6</li>
                                                                                       <li class="ui-widget-content">7</li>
                                                                                  <li class="ui-widget-content">8</li>
                                                                                       <li class="ui-widget-content">9</li>
                                                                                       <li class="ui-widget-content">10</li>
                                                                                       <li class="ui-widget-content">11</li>
                                                                                       <li class="ui-widget-content">12</li>
                                                                                       <li class="ui-widget-content">13</li>
                                                                                       <li class="ui-widget-content">14</li>
                                                                                  <li class="ui-widget-content">15</li>
                                                                                       <li class="ui-widget-content">16</li>
                                                                                       <li class="ui-widget-content">17</li>
                                                                                       <li class="ui-widget-content">18</li>
                                                                                       <li class="ui-widget-content">19</li>
                                                                                       <li class="ui-widget-content">20</li>
                                                                                       <li class="ui-widget-content">21</li>
                                                                                  <li class="ui-widget-content">22</li>
                                                                                  <li class="ui-widget-content">23</li>
                                                                                  <li class="ui-widget-content">24</li>
                                                                                    </ol>
                                                                                    <input type="hidden" id="bad_times" name="bad_times" value="" />
                                                                        </div>
                                                                        <br/><br/> <br/><br/>
                                                                        <div id="buttonClear">Click here to clear</div><br/>
                                                                        <p>This will work for many players, but not for all. Leave it blank, if, for example, you do alternating shifts at work and your sleeping schedule looks different every week.  </p>


                                                                        <hr style="height:1px;border:none;color:#333;background-color:#333;"/>

                        <p>Below you may specify detailed time control preferences. If your and
                           your opponent's time control preference matches, it will be used for
                           your game. If it does not, the default (45 45) will be used.</p>
                        <label for="def_time"></label>
                        <input type="checkbox" id="def_time" name="def_time" value="def_time" checked>I want the default time control (45 45) to be used in all my games. (Untick this box for detailed preferences)<br>
                        <div id="var1">
                          <input type="hidden" id="time_order" name="time_order" value="" />
                          <label for="sortable">My preferences are (you can drag and drop elements):</label>
                          <table border="1" cellpadding="10">
                          <tr><td><center>Preferred time controls,<br>
                                          in this order:</center></td><td><center>Excluded time controls:</center></td></tr>
                          <tr><td><ul id="sortable" class="connectedSortable">
                            <li id="45_45" class="ui-state-default"> 45 45  <img src="/static/images/clock.png"/></li>
                            <li id="120_30" class="ui-state-default">120 30 <img src="/static/images/clock.png"/></li>
                            <li id="90_30" class="ui-state-default"> 90 30  <img src="/static/images/clock.png"/></li>
                            <li id="75_0" class="ui-state-default"> 75 0   <img src="/static/images/clock.png"/></li>
                            <li id="50_10" class="ui-state-default"> 50 10  <img src="/static/images/clock.png"/></li>
                          </ul>
                          </td><td>
                          <ul id="sortable2" class="connectedSortable">

                                                    </ul>
                                </td></tr> </table>
                                <p>* The default (45 45) cannot be ruled out.</p>
                       <!-- <label for="op_dec"></label>
                                                <input type="checkbox" id="op_dec" name="op_dec" value="op_dec">I leave it to my opponent to decide on one of above time controls. (If yes, tick the
                                                                                                                                                                                   box for further options!)<br>
                         <div id="var2">
                         <label for="insist">But I insist on the following:*</label>
        <input type="checkbox" id="insistno120" name="insistno120" value="no120">no 120 30<br/></input>
        <input type="checkbox" id="insistno90" name="insistno90" value="no90">no 90 30<br/></input>
        <input type="checkbox" id="insistno75" name="insistno75" value="no75">no 75 0<br/> </input>
        <input type="checkbox" id="insistno45" name="insistno45" value="no45">no 45 10<br/> </input>


                        </div>  -->
                        </div>


        </fieldset>
        <p>
            <input id="SaveAccount" type="submit" value="Submit form" />
        </p>
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