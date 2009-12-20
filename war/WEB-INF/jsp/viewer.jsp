<% String s = null; 
if ((s=request.getParameter("l"))!=null) { %>
<title>Viewer Deluxe</title>
<APPLET codebase="." archive="/static/Viewer-Deluxe.jar" code="ChessBoard.class" width="731" height="600">
  <PARAM name=PgnGameFile value="<%=s%>">
  <PARAM name=LightSquares value=F3DCC2>
  <PARAM name=DarkSquares value=DDA37B>
  <PARAM name=Background value=CCCCCC>
  <PARAM name=ImagesFolder value=images>
  <PARAM name=PuzzleMode value="off">
  <PARAM name=MayScript value="on">

  Your browser is completely ignoring the &lt;APPLET&gt; tag!
</APPLET>

<% } %>