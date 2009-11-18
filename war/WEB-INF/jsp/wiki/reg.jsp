<%@ page import="net.rwchess.site.data.RWMember"%>

<div id="user-menu">
<ul>
    <% RWMember user = (RWMember) pageContext.getSession().getAttribute("user"); 
       if (user == null) {  
    %>	
		<li><a href="/wiki/Special:Login" title="Special:Login">Login</a></li>
	<% }
	   else { %>
		<li><a href="/wiki/Special:Exit" title="Special:Exit">Exit</a></li>
	<% } %>

</ul>
</div>