<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="nav.home"/>
<c:set var="active" value="home"/>
<%@ include file="/WEB-INF/jspf/header.jspf" %>

<h1><fmt:message key="home.title"/></h1>
<p><fmt:message key="home.lead"/></p>
<p><a class="btn" href="${ctx}/issues"><fmt:message key="home.open"/></a></p>

<h2><fmt:message key="home.map"/></h2>
<div class="table-wrap">
  <table class="map">
    <thead>
      <tr><th><fmt:message key="home.map.lab"/></th><th><fmt:message key="home.map.where"/></th></tr>
    </thead>
    <tbody>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/about.jsp"><fmt:message key="home.map.web1"/></a></td><td><fmt:message key="home.map.web1.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><fmt:message key="home.map.web2"/></td><td><fmt:message key="home.map.web2.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/servlet/issues"><fmt:message key="home.map.web3"/></a></td><td><fmt:message key="home.map.web3.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="?lang=en"><fmt:message key="home.map.web4"/></a></td><td><fmt:message key="home.map.web4.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/issues"><fmt:message key="home.map.web5"/></a></td><td><fmt:message key="home.map.web5.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/login"><fmt:message key="home.map.web6"/></a></td><td><fmt:message key="home.map.web6.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/ask-reader"><fmt:message key="home.map.web7"/></a></td><td><fmt:message key="home.map.web7.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><a href="${ctx}/gwt.html"><fmt:message key="home.map.web8"/></a></td><td><fmt:message key="home.map.web8.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><fmt:message key="home.map.web9"/></td><td><fmt:message key="home.map.web9.t"/></td></tr>
      <tr><td data-label="<fmt:message key="home.map.lab"/>"><fmt:message key="home.map.oop"/></td><td><fmt:message key="home.map.oop.t"/></td></tr>
    </tbody>
  </table>
</div>

<%@ include file="/WEB-INF/jspf/footer.jspf" %>
