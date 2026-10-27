<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Ошибка</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h1>Ошибка</h1>
<p class="error"><%= request.getAttribute("message") == null ? "Запрос не выполнен" : request.getAttribute("message") %></p>
<p><a href="ask.jsp">Вернуться к выбору читателя</a></p>
</body>
</html>
