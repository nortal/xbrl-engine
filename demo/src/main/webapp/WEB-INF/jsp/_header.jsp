<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%
    response.setHeader("Cache-Control","no-cache");
    response.setHeader("Pragma","no-cache");
    response.setHeader("Expires","0");

%><%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form"   uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="xbrl"   uri="http://xbrl-engine-fragment" %>
<c:set var="lang" value="en" />
<html lang="${lang}">
<head>
    <title>XBRL Engine JSP Demo</title>
    <link type="text/css" href="/css/demo.css" rel="stylesheet" />
    <link type="text/css" href="/css/xbrl.css" rel="stylesheet" />
    <link type="text/css" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0-beta/css/bootstrap.min.css" rel="stylesheet">
    <link type="text/css" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet">
</head>
<body>
<nav class="navbar navbar-expand-md navbar-dark bg-dark">
    <a class="navbar-brand" href="#">XBRL Engine Demo</a>

    <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarsExampleDefault" aria-controls="navbarsExampleDefault" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navbarsExampleDefault">
        <ul class="navbar-nav mr-auto">
            <li class="nav-item">
                <a class="nav-link" href="/">Start</a>
            </li>
            <c:if test="${not empty reportId}">
                <li class="nav-item">
                    <a class="nav-link" href="/${reportId}">Select</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/${reportId}/list">Forms</a>
                </li>
            </c:if>
        </ul>
    </div>
</nav>