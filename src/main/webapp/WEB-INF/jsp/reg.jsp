<%@ page import="net.rwchess.persistent.Member"%>
<%@ page import="org.springframework.security.core.context.SecurityContextHolder"%>

<div id="user-menu">
<br/>
<ul>
    <li><span style="font-size: 115%"><a href="/">Home</a></span></li>
    <li><span style="font-size: 115%"><a href="https://snailbucket.org/wiki/Help">Help</a></span></li>
    <li><span style="font-size: 115%; padding-right: 100px;"><a href="https://snailbucket.org/wiki/About">About</a></span></li>


    <% Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       if (user instanceof String) {
    %>
        <li><a href="/spring_security_login" title="Special:Login"><span style="font-size: 100%">Login</span></a></li>
    <% }
       else { %>
        <li><a href="/j_spring_security_logout"><span style="font-size: 100%">Exit [<%=((Member)user).getUsername() %>]</span></a></li>
        <li><a href="/profile"><span style="font-size: 100%">Profile</span></a></li>
    <% } %>
</ul>
<br/>
</div>
