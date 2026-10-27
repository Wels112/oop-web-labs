<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page import="edu.java.web.IssueStore" %>
<%
  // Читатель, сохранённый в сессии сервлетом ReaderProcessor
  String reader = (String) session.getAttribute("reader");
  if (reader == null) {
    response.sendRedirect(request.getContextPath() + "/ask.jsp");
    return;
  }
  request.setAttribute("reader", reader);
  request.setAttribute("issues", IssueStore.byReader(reader));
  // Читатель, сохранённый в Cookie при прошлом заходе
  String lastCookie = "нет";
  if (request.getCookies() != null) {
    for (jakarta.servlet.http.Cookie c : request.getCookies()) {
      if ("book.reader".equals(c.getName())) {
        lastCookie = java.net.URLDecoder.decode(c.getValue(), "UTF-8");
      }
    }
  }
  request.setAttribute("cookieReader", lastCookie);
%>
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
<p>Читатель в сессии: <b><c:out value="${reader}"/></b>. Читатель в cookie: <b><c:out value="${cookieReader}"/></b>.</p>
<p><a href="ask.jsp">Другой читатель</a></p>
</body>
</html>
