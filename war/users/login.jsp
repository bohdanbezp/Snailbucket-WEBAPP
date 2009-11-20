<form action="/actions/login" method="post">
<table class="wwFormTable">
	<tr>
		<td class="tdLabel"><label for="Login_username" class="label">Username:</label></td>

		<td><input type="text" name="login" value=""
			id="Login_username" /></td>
	</tr>

	<tr>
		<td class="tdLabel"><label for="Login_password" class="label">Password:</label></td>
		<td><input type="password" name="password" id="Login_password" /></td>
	</tr>

	<tr>
		<td colspan="2">
		<div align="right"><input type="submit" id="Login_0"
			value="Submit" /></div>
		</td>
	</tr>
    
</table>
<input type="hidden" name="ref" value="<%=request.getParameter("ref") %>"/>

<% if (session.getAttribute("LoginError") != null)  { 	%>
       <%= session.getAttribute("LoginError")%>
    <% 
           session.removeAttribute("LoginError"); 
       } %>    
</form>