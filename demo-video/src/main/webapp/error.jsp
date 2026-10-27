<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="error.title"/>
<c:set var="active" value="error"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="error.title"/></h1>
<p>HTTP <c:out value="${pageContext.errorData.statusCode}"/></p>
<p><a class="btn" href="${ctx}/"><fmt:message key="error.back"/></a></p>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
