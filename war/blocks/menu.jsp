<%@ page import="net.rwchess.site.data.RWMember"%>

<div id="menu">

<ul>
    
    <% 
    RWMember m = (RWMember) session.getAttribute("user");
    if (m != null) {
    	if (m.getGroup() >= RWMember.MODERATOR) { %>
    	   <li style="color:red"><a href="/users/edit">User management</a></li>	
    <%	} %>
    	 <li><a href="/users/profile">Profile</a></li>	
    <%}
    
    %>
     
	<li><a href="/">Home</a></li>
	<li><a href="/members">Members</a></li>
	<li><a href="/wiki">Wiki</a></li>	
	<li><a href="/archive">Archive</a></li>
	<li><a href="/wiki/about">About</a></li>
	<!-- <li><a href="/links.jsp">Links</a></li> -->		
</ul>
</div>
