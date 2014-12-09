<%@ page import="net.rwchess.persistent.dao.WikiPageDAOHib"%>
<%@ page import="net.rwchess.persistent.WikiPage"%>

<%
 WikiPage pgg = WikiPageDAOHib.getCachedDao().getWikiPageByName("Special:TopMenu");
%>

<%=pgg.getHtmlText()%>


