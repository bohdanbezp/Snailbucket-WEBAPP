<%@ taglib uri="/WEB-INF/tags" prefix="rws"%>

<div id="user-menu">
<ul>

	<rws:UserCheck status="anonymous" checkFor="equality">
		<li><a href="/wiki/Special:Login" title="Special:Login">Login</a></li>
	</rws:UserCheck>
	<rws:UserCheck status="anonymous" checkFor="inequality">
		<li><a href="/wiki/Special:Exit" title="Special:Exit">Exit</a></li>
	</rws:UserCheck>

</ul>
</div>