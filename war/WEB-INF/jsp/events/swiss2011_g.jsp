<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<p>Please read the <a href="/wiki/RWSwiss2011%20TourneyGuide">tourney guide</a> before registering.</p>
<form name="form1" id="form1" method="post" action="/signswissguest">
      <table border="0" align="center">
      <tr>           
      <td><label for="user" class="label">FICS username:</label></td>
      <td><input type="text" id="user" name="user" maxlength="25"/></td>
      </tr>      
      <tr> 
      <td><label for="email" class="label">Email:</label></td>
      <td><input type="text" id="email" name="email" maxlength="40"/></td>
      </tr>    
      <tr> 
      <td><label for="country" class="label">Country (<a href="http://www.spoonfork.org/isocodes.html">ISO code</a>; optional):</label></td>
      <td><input type="text" id="country" name="country" maxlength="2"/></td>
      </tr> 
      </table>
      <br/><input type="submit" value="Sign up" />
      </form>
      <br/>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>