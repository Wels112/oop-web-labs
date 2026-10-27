<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Картотека выдач</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h1>Картотека выдач</h1>
<p>Лабораторная работа № 2: web-проект, созданный по архетипу Maven и собранный командой <code>mvn install</code>.</p>
<p>Приложение развёрнуто как war-файл по адресу <b><%= request.getContextPath() %>/</b>.</p>
<p>Сервер: <%= application.getServerInfo() %>. Время: <%= new java.util.Date() %>.</p>
</body>
</html>
