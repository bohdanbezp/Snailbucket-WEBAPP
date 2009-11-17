<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<form class="news-form" action="/actions/post" method="POST">    
    <input name="forum" value="news" type="hidden"/>
    <p>       
		<input name="title" value="" type="text"/>
		 <label>Title</label>
    </p>
    <p><textarea name="message" cols="54" rows="6" tabindex="3"></textarea></p>
    <div align="center">
    <p><button type="submit">Submit</button></p>    
    </div>    
</form>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>