<%@ page import="net.rwchess.utils.UsefulMethods"%>
<%@ page import="net.rwchess.persistent.dao.WikiPageDAOHib"%>
<%@ page import="net.rwchess.persistent.WikiPage"%>
<%@ page import="net.rwchess.services.DisplayPositionService"%>
<%@ page import="net.rwchess.services.DisplayPositionService.PosInfo"%>

<%
 WikiPage pgg = WikiPageDAOHib.getCachedDao().getWikiPageByName("Special:LeftMenu");
 if (pgg != null) {
%>

<%=pgg.getHtmlText().replaceAll("CURR_ROUND", Integer.toString(1)).replaceAll("PREV_ROUND", Integer.toString(0))%>

<br/>
<center>
<% if (DisplayPositionService.info != null) { %>
<p><i>Random game position</i><br/>
<%=DisplayPositionService.info.gameString%>

</p>
<img src="/wikiImg/fen.png"/>
<p>after <%=DisplayPositionService.info.lastMove%></p>
<% } }%>
</center>