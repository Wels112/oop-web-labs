<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Tomcat работает</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h1>Web-сервер Apache Tomcat работает</h1>
<p>Лабораторная работа № 1: установка и настройка среды разработки и исполнения web-приложения.</p>
<table>
<tr><th>Сервер</th><td><%= application.getServerInfo() %></td></tr>
<tr><th>Servlet API</th><td><%= application.getMajorVersion() %>.<%= application.getMinorVersion() %></td></tr>
<tr><th>Версия Java</th><td><%= System.getProperty("java.version") %></td></tr>
<tr><th>Путь приложения</th><td><%= request.getContextPath() %></td></tr>
<tr><th>Время сервера</th><td><%= new java.util.Date() %></td></tr>
</table>
</body>
</html>
