<%@ taglib uri="/WEB-INF/tags" prefix="rws" %>

<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<rws:UserCheck status="anonymous" checkFor="equality">
    <jsp:include page="/users/login.jsp"></jsp:include>
</rws:UserCheck>
<rws:UserCheck status="anonymous" checkFor="inequality">
    <jsp:include page="newsarea.jsp"></jsp:include>
</rws:UserCheck>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>


