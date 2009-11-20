<%@ page import="net.rwchess.site.data.RWMember"%>

<div id="user-menu">
<ul>
    <% RWMember user = (RWMember) pageContext.getSession().getAttribute("user"); 
       if (user == null) {  
    %>	
		<li><a href="/wiki/Special:Login?ref=<%=request.getRequestURI()%>" title="Special:Login">Login</a></li>
	<% }
	   else { %>
		<li><a href="/actions/exit">Exit [<%=user.getUsername() %>]</a></li>
	<% } %>

</ul>
</div>