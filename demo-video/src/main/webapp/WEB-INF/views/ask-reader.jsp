<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="ask.title"/>
<c:set var="active" value="ask"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="ask.title"/></h1>
<p><fmt:message key="ask.lead"/></p>
<c:if test="${not empty lastReader}">
  <p class="hint"><fmt:message key="ask.last"/>: <strong><c:out value="${lastReader}"/></strong></p>
</c:if>
<form class="narrow-form" method="post" action="${ctx}/ask-reader">
  <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
  <label><fmt:message key="filter.reader"/>
    <select name="reader">
      <option value="Все читатели"><fmt:message key="ask.all"/></option>
      <c:forEach var="r" items="${readers}">
        <option value="<c:out value='${r}'/>"<c:if test="${r == lastReader}"> selected</c:if>><c:out value="${r}"/></option>
      </c:forEach>
    </select>
  </label>
  <button class="btn" type="submit"><fmt:message key="ask.submit"/></button>
</form>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
