<%@ page import="net.rwchess.site.data.DAO"%>
<%@ page import="net.rwchess.site.utils.UsefulMethods"%>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<% if (!DAO.playsInSwiss(UsefulMethods.getUsername(session))) { %>
      <form name="form1" id="form1" method="post" action="/actions/signswiss">
      <br/>
      <input type="submit" value="Sign up" />
      </form>
<% } %>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>