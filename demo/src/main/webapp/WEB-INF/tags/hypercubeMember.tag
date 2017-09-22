<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="reportForm" type="com.nortal.xbrl.view.model.ReportForm" required="true"%>
<%@ attribute name="context" type="com.nortal.xbrl.metamodel.XbrlContext" required="true"%>
<%@ attribute name="axis" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="axisDimension" type="com.nortal.xbrl.metamodel.meta.DimensionEntry" required="true"%>
<%@ attribute name="lineItem" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="reportMetamodel" type="com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel" required="true"%>
<%@ attribute name="lang" type="java.lang.String" required="true" %>

<spring:eval var="dateFormat" expression="T(com.nortal.xbrl.constants.Constants).DEFAULT_DATE_PATTERN" />
<c:set var="isTotal" value="${reportMetamodel.isTotalPresentation(lineItem) ? true : false}" />

<tr>
    <td colspan="${lineItem['abstract'] ? axisDimension.getDomainElementCount() + 2 : '1'}">
        <div class="level_${lineItem.level} ${lineItem['abstract'] ? 'level_abstract' : ''} ${isTotal ? 'total_label' : ''}">
            <c:if test="${isTotal}">
                <span class="req">*</span>
            </c:if>
            ${lineItem.getPreferredLabel(lang)}
            <c:if test="${lineItem.period == 'INSTANT'}">
                <c:if test="${lineItem.preferredLabelType == 'PERIOD_START'}">
                    <fmt:formatDate type="date" pattern="${dateFormat}" value="${context.getPeriodStartDate()}" />
                </c:if>
                <c:if test="${lineItem.preferredLabelType == 'PERIOD_END'}">
                    <fmt:formatDate type="date" pattern="${dateFormat}" value="${context.getPeriodEndDate()}" />
                </c:if>
            </c:if>
        </div>
    </td>
    <c:if test="${not lineItem['abstract']}">
        <td>
            <xbrl:references presentationEntry="${lineItem}" lang="${lang}" />
        </td>
        <xbrl:hypercubeMemberAxis reportForm="${reportForm}" context="${context}" axis="${axis}" lineItem="${lineItem}" reportMetamodel="${reportMetamodel}"/>
    </c:if>
</tr>
<c:if test="${!empty lineItem.children}">
    <c:forEach var="lineItem" items="${lineItem.children}">
        <xbrl:hypercubeMember reportForm="${reportForm}" context="${context}" axis="${axis}"
                axisDimension="${axisDimension}" lineItem="${lineItem}" reportMetamodel="${reportMetamodel}" lang="${lang}"/>
    </c:forEach>
</c:if>