<!DOCTYPE html>
<html>
<head>
<title>Spring MVC Action Resolver Demo</title>
</head>
<body>
	<h1>Welcome! Here are some urls generated using the action resolver:</h1>
	<ul>
		<li>${actionResolver.uri('homePage')}</li>
		<li>${actionResolver.uri('RENAMED_PAGE')}</li>
		<li>${actionResolver.uri('pathVariablePage').pathParam('Hello').pathParam('World')}</li>
		<li>${actionResolver.uri('redirect')}</li>
	</ul>
</body>
</html>