<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="presentationEntry" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="lang" type="java.lang.String" required="true"%>

<c:if test="${not empty presentationEntry.labels[lang].documentationLabel}">
    <c:set var="references" value="" />
    <c:forEach items="${presentationEntry.references}" var="reference">
        <c:if test="${!references.isEmpty()}">
            <c:set var="references" value="${references}, " />
        </c:if>
        <c:choose>
            <c:when test="${empty reference.uri}">
                <fmt:formatDate type='date' pattern='yyyy-MM-dd' value='${reference.issueDate}' var="issueDate"/>
                <c:set var="references" value="${references} ${reference.name} ${reference.number}
                        ${issueDate} §${reference.paragraph} ${reference.subParagraph}"/>
            </c:when>
            <c:otherwise>
                <fmt:formatDate type='date' pattern='yyyy-MM-dd' value='${reference.issueDate}' var="issueDate"/>
                <c:set var="references" value="${references} <a target='_blank' href='${reference.uri}'>${reference.name} ${reference.number}
                        ${issueDate} §${reference.paragraph} ${reference.subParagraph}</a>"/>
            </c:otherwise>
        </c:choose>
    </c:forEach>

    <a class="btn btn-default btn-sm form-control-help" data-html="true" data-toggle="popover" data-content="${presentationEntry.labels[lang].documentationLabel}
        <c:if test='${!references.isEmpty()}'>
            <spring:message code="reference.see" />
            ${references}
        </c:if>">
        <span class="fa fa-question"></span>
    </a>
</c:if>


