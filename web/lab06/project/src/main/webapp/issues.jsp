<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ru">
<head>
<meta charset="UTF-8">
<title>Список выдач</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
<h1>Список выдач читателя <c:out value="${reader}"/></h1>
<c:choose>
  <c:when test="${empty issues}">
    <p class="empty">Выдач не найдено</p>
  </c:when>
  <c:otherwise>
    <table>
      <tr><th>Книга</th><th>Дата выдачи</th><th>Срок возврата</th><th>Возвращена</th></tr>
      <c:forEach var="i" items="${issues}">
        <tr>
          <td><c:out value="${i.book}"/></td>
          <td><c:out value="${i.issued}"/></td>
          <td><c:out value="${i.due}"/></td>
          <td>${i.returned ? 'Да' : 'Нет'}</td>
        </tr>
      </c:forEach>
    </table>
  </c:otherwise>
</c:choose>
<p><a href="ask.jsp">Другой читатель</a></p>
</body>
</html>
