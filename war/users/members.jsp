<%@ page import="java.util.List"%>
<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.data.RWMember"%>

<%
out.println(DAO.getAllMembersTable());
%>
