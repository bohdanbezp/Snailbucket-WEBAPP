<%@ page import="net.rwchess.site.data.RWMember"%>

<div id="user-menu">
<ul>
    <% RWMember user = (RWMember) pageContext.getSession().getAttribute("user"); 
       if (user == null) {  
    %>	
		<li><a href="/wiki/Special:Login" title="Special:Login">Login</a></li>
	<% }
	   else { %>
		<li><a href="/actions/exit">Exit [<%=user.getUsername() %>]</a></li>
		<li><a href="/users/profile">Profile</a></li>	
		<% if (user.getGroup() >= RWMember.MODERATOR) { %>
    	   <li style="color:red"><a href="/users/edit">User management</a></li>	
        <% } %>
	<% } %>

</ul>
</div>