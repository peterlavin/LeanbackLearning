<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>

		<script src="javascript/jquery-2.0.0.js"></script>


<script type="text/javascript">

	var name = "test_string";
	
	$(document).ready(function() {
						

					$('#submit').click(function(event) {
						
						console.log("Test line in submit.click(fn(event))");

												$.get('SequenceServlet', {
																	name : name
																},
																function(responseText) {

																	console.log("Returned text:\n" + responseText);

																}); //end of function(responseText) brace
											
										}); // end submit.click function(event)
						
					}); // end document.ready function

</script>

</head>
<body>

<!-- 	Short working example of a dynamic web service using Javascript to call a Java Servlet -->

	<button type="button" id="submit">Button</button>

</body>
</html>
