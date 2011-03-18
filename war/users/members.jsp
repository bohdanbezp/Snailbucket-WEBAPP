<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.RWMember"%>

<h2>Members</h2>
<%
out.println(DAO.getAllMembersTable());
%>
<h2>Swiss Guests</h2>
<%
out.println(DAO.getAllSwissGuestsTable());
%>