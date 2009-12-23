<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<form action="/actions/addmember" method="post">
<table class="wwFormTable">
	<tr>
		<td class="tdLabel"><label for="Login_username" class="label">Username:</label></td>

		<td><input type="text" name="login" value=""
			id="Login_username" /></td>
	</tr>

	<tr>
		<td class="tdLabel"><label for="Login_password" class="label">Temporary password:</label></td>
		<td><input type="password" name="password" id="Login_password" /></td>
	</tr>
	
	<tr>
		<td class="tdLabel"><label for="Login_country" class="label">Country:</label></td>
		<td><input type="text" name="country" id="Login_country" size="2" /></td>
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