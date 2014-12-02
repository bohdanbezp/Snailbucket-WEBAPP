<script type="text/javascript">
Set_Cookie( 'test', 'none', '', '/', '', '' );
if ( Get_Cookie( 'test' ) )
{
	cookie_set = true;
	Delete_Cookie('test', '/', '');
}
else
{
	document.write( 'Please enable cookies to sucessfully use the site' );
	cookie_set = false;
}
</script>
