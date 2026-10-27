<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="login.title"/>
<c:set var="active" value="login"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="login.title"/></h1>
<c:if test="${loginError}">
  <div class="note err" role="alert"><fmt:message key="login.error"/></div>
</c:if>
<form class="narrow-form" method="post" action="${ctx}/login">
  <input type="hidden" name="next" value="<c:out value='${next}'/>">
  <label><fmt:message key="login.username"/>
    <input name="username" value="<c:out value='${username}'/>" autocomplete="username" required autofocus>
  </label>
  <label><fmt:message key="login.password"/>
    <input name="password" type="password" autocomplete="current-password" required>
  </label>
  <button class="btn" type="submit"><fmt:message key="login.submit"/></button>
</form>
<p class="hint" style="margin-top:16px"><fmt:message key="login.hint"/></p>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
