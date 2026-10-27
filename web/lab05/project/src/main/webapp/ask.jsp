<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Выбор читателя</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h1>Чьи книги показать?</h1>
<form action="process" method="post">
  <p>Читатель:<br>
  <select name="reader">
    <option>Иванов Пётр</option>
    <option>Смирнова Анна</option>
    <option>Кузнецов Илья</option>
  </select></p>
  <button type="submit">Показать книги</button>
</form>
</body>
</html>
