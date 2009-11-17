<jsp:include page="/blocks/top.jsp"></jsp:include>
<jsp:include page="/blocks/currevents.jsp"></jsp:include>

<p>Currently you can upload a file with description. It might be anything
chess-related or may be interesting to club members. Please don't violate
copyright and other laws.</p><br/>

<form name="filesForm" action="/actions/upload" method="post" enctype="multipart/form-data">
<input type="file" name="downldFile"><br/><br/>
<textarea name="description" cols="54" rows="6" tabindex="3"></textarea>
<br/><br/>
<input type="submit" name="Submit" value="Upload File">
</form>

<jsp:include page="/blocks/bottom.jsp"></jsp:include>