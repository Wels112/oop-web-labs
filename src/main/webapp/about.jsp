<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="about.title"/>
<c:set var="active" value="about"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="about.title"/></h1>
<dl class="facts">
  <dt><fmt:message key="about.server"/></dt>
  <dd><c:out value="${pageContext.servletContext.serverInfo}"/></dd>
  <dt><fmt:message key="about.servlet"/></dt>
  <dd><c:out value="${pageContext.servletContext.majorVersion}.${pageContext.servletContext.minorVersion}"/></dd>
  <dt><fmt:message key="about.java"/></dt>
  <dd><%= System.getProperty("java.version") %></dd>
  <dt><fmt:message key="about.context"/></dt>
  <dd><c:out value="${empty ctx ? '/' : ctx}"/></dd>
  <dt><fmt:message key="about.session"/></dt>
  <dd><c:out value="${pageContext.session.id}"/></dd>
  <dt><fmt:message key="about.time"/></dt>
  <dd><%= new java.util.Date() %></dd>
</dl>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
