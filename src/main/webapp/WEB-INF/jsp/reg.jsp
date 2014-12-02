<%@ page import="net.rwchess.persistent.Member"%>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%>

<div id="user-menu">
<ul>
    <% Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if (user instanceof String) {
    %>	
		<li><a href="/spring_security_login" title="Special:Login">Login</a></li>
	<% }
	   else { %>
		<li><a href="/j_spring_security_logout">Exit [<%=((Member)user).getUsername() %>]</a></li>
		<li><a href="/profile">Profile</a></li>
	<% } %>

</ul>
</div>