<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<p>As a guest member you have to change your automatically generated password as an 
indication of your approval by TD.</p>

<form action="/actions/changepasswd" method="post">
<table class="wwFormTable">
	<tr>
		<td class="tdLabel"><label for="Login_username" class="label">Username:</label></td>

		<td><%=session.getAttribute("login") %></td>
	</tr>

	<tr>
		<td class="tdLabel"><label for="Login_password" class="label">New password:</label></td>
		<td><input type="password" name="password" id="Login_password" /></td>
	</tr>

	<tr>
		<td colspan="2">
		<div align="right"><input type="submit" id="Login_0"
			value="Submit" /></div>
		</td>
	</tr>
    
</table>
</form>


<jsp:include page="/blocks/bottom.jsp"></jsp:include>