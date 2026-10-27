<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Web, лабораторная работа № 5: страница JSP. Данные готовит контроллер
     IssuesController и передаёт в атрибутах запроса rows, readers, selectedReader. --%>
<%!
  /** Объявление JSP: метод класса страницы, склоняет слово «запись». */
  private String recordsWord(int n) {
    int tail = n % 100;
    if (tail >= 11 && tail <= 14) return "записей";
    switch (n % 10) {
      case 1: return "запись";
      case 2: case 3: case 4: return "записи";
      default: return "записей";
    }
  }
%>
<%
  /* Скриптлет: число найденных строк берётся прямо из атрибута запроса. */
  java.util.List<?> found = (java.util.List<?>) request.getAttribute("rows");
  int total = found == null ? 0 : found.size();
%>
<c:set var="pageTitle" value="issues.title"/>
<c:set var="active" value="issues"/>
<c:set var="isLibrarian" value="${sessionScope.role == 'LIBRARIAN'}"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="issues.title"/></h1>

<c:if test="${not empty flash}"><div class="note ok" role="status"><c:out value="${flash}"/></div></c:if>
<c:if test="${not empty error}"><div class="note err" role="alert"><c:out value="${error}"/></div></c:if>

<form class="row" method="get" action="${ctx}/issues">
  <input type="hidden" name="search" value="1">
  <label><fmt:message key="filter.reader"/>
    <select name="reader">
      <option value="Все читатели"><fmt:message key="ask.all"/></option>
      <c:forEach var="r" items="${readers}">
        <option value="<c:out value='${r}'/>"<c:if test="${r == selectedReader}"> selected</c:if>><c:out value="${r}"/></option>
      </c:forEach>
    </select>
  </label>
  <label><fmt:message key="filter.book"/>
    <input name="book" value="<c:out value='${book}'/>">
  </label>
  <button class="btn" type="submit"><fmt:message key="filter.search"/></button>
  <a class="btn quiet" href="${ctx}/issues?reader=Все+читатели"><fmt:message key="filter.reset"/></a>
  <a class="btn quiet" href="${ctx}/ask-reader"><fmt:message key="issues.pick"/></a>
</form>

<c:if test="${isLibrarian}">
  <details class="panel">
    <summary><fmt:message key="issues.add"/></summary>
    <form method="post" action="${ctx}/issues">
      <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
      <input type="hidden" name="action" value="add">
      <label><fmt:message key="col.reader"/>
        <select name="reader">
          <c:forEach var="r" items="${readers}"><option><c:out value="${r}"/></option></c:forEach>
        </select>
      </label>
      <label><fmt:message key="col.book"/><input name="book" required></label>
      <label><fmt:message key="col.issued"/><input name="issued" placeholder="<fmt:message key='issues.date.hint'/>" required></label>
      <label><fmt:message key="col.due"/><input name="due" placeholder="<fmt:message key='issues.date.hint'/>" required></label>
      <label class="check"><input type="checkbox" name="returned"><fmt:message key="issues.returned"/></label>
      <button class="btn" type="submit"><fmt:message key="issues.add.submit"/></button>
    </form>
  </details>
</c:if>

<c:choose>
  <c:when test="${empty rows}">
    <p class="empty"><fmt:message key="list.empty"/></p>
  </c:when>
  <c:otherwise>
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th><fmt:message key="col.reader"/></th>
            <th><fmt:message key="col.book"/></th>
            <th><fmt:message key="col.issued"/></th>
            <th><fmt:message key="col.due"/></th>
            <th><fmt:message key="col.returned"/></th>
            <c:if test="${isLibrarian}"><th><fmt:message key="col.actions"/></th></c:if>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${rows}">
            <c:set var="it" value="${row.issue}"/>
            <tr>
              <td data-label="<fmt:message key='col.reader'/>"><c:out value="${it.reader}"/></td>
              <td data-label="<fmt:message key='col.book'/>"><c:out value="${it.book}"/></td>
              <td data-label="<fmt:message key='col.issued'/>"><c:out value="${it.issued}"/></td>
              <td data-label="<fmt:message key='col.due'/>"><c:out value="${it.due}"/></td>
              <td data-label="<fmt:message key='col.returned'/>">
                <c:choose>
                  <c:when test="${it.returned == 'Да'}"><span class="tag yes"><fmt:message key="yes"/></span></c:when>
                  <c:otherwise><span class="tag no"><fmt:message key="no"/></span></c:otherwise>
                </c:choose>
              </td>
              <c:if test="${isLibrarian}">
                <td class="actions">
                  <details class="edit">
                    <summary><fmt:message key="issues.edit"/></summary>
                    <form method="post" action="${ctx}/issues">
                      <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                      <input type="hidden" name="action" value="update">
                      <input type="hidden" name="index" value="${row.index}">
                      <label><fmt:message key="col.reader"/>
                        <select name="reader">
                          <c:forEach var="r" items="${readers}"><option<c:if test="${r == it.reader}"> selected</c:if>><c:out value="${r}"/></option></c:forEach>
                        </select>
                      </label>
                      <label><fmt:message key="col.book"/><input name="book" value="<c:out value='${it.book}'/>" required></label>
                      <label><fmt:message key="col.issued"/><input name="issued" value="<c:out value='${it.issued}'/>" required></label>
                      <label><fmt:message key="col.due"/><input name="due" value="<c:out value='${it.due}'/>" required></label>
                      <label class="check"><input type="checkbox" name="returned"<c:if test="${it.returned == 'Да'}"> checked</c:if>><fmt:message key="issues.returned"/></label>
                      <button class="btn small" type="submit"><fmt:message key="issues.edit.submit"/></button>
                    </form>
                  </details>
                  <form method="post" action="${ctx}/issues" onsubmit="return confirm('<fmt:message key='issues.confirm'/>')">
                    <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                    <input type="hidden" name="action" value="delete">
                    <input type="hidden" name="index" value="${row.index}">
                    <button class="btn danger small" type="submit"><fmt:message key="issues.delete"/></button>
                  </form>
                </td>
              </c:if>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:otherwise>
</c:choose>
<p class="hint" style="margin-top:12px"><fmt:message key="issues.count"/>: <%= total %><c:if test="${locale.language == 'ru'}"> (<%= recordsWord(total) %>)</c:if></p>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
