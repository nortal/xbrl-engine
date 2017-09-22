<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ attribute name="reportForm" type="com.nortal.xbrl.view.model.ReportForm" required="true"%>
<%@ attribute name="formMetamodel" type="com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel" required="true"%>
<%@ attribute name="contexts" type="java.util.List" required="true"%>
<%@ attribute name="lang" type="java.lang.String" required="true" %>

<c:set var="instance" value="${reportForm.xbrlInstance}"></c:set>
<c:set var="presentationEntries" value="${formMetamodel.getFlatPresentation()}" />

<table class="table table-hover">
    <thead>
        <tr>
            <th></th>
            <th></th>
            <c:forEach items="${contexts}" var="context">
                <c:if test="${not context.hasExplicitMember()}">
                    <th class="text-center">
                        <c:choose>
                            <c:when test="${empty context.periodLabel}">
                                <fmt:message key="context.period.label.forever"/>
                            </c:when>
                            <c:otherwise>
                                ${context.periodLabel}
                            </c:otherwise>
                        </c:choose>
                    </th>
                </c:if>
            </c:forEach>
        </tr>
    </thead>
    <tbody>
        <c:set var="count" value="0" scope="page" />
        <c:forEach items="${presentationEntries}" var="presentationEntry">
            <tr>
                <td colspan="${presentationEntry['abstract'] ? contexts.size() + 2 : '1'}">
                    <c:set var="isTotal" value="${formMetamodel.isTotalPresentation(presentationEntry) ? true : false}" />
                    <div class="level_${presentationEntry.level} ${isTotal ? 'total_label' : ''} ${presentationEntry['abstract'] ? 'level_abstract' : ''}">
                        <c:if test="${isTotal}">
                            <span class="req">*</span>
                        </c:if>
                        ${presentationEntry.getPreferredLabel(lang)}
                    </div>
                </td>
                <c:if test="${not presentationEntry['abstract']}">
                    <td>
                        <xbrl:references presentationEntry="${presentationEntry}" lang="${lang}" />
                    </td>
                    <c:forEach items="${contexts}" var="context">
                        <c:if test="${not context.hasExplicitMember()}">
                            <c:if test="${presentationEntry.type=='TEXT_BLOCK'}">
                                <c:set var="areaClass" value="area-cell" />
                            </c:if>
                            <td class="${areaClass}">
                                <c:if test="${!presentationEntry['abstract']}">
                                    <c:set var="valueEntry" value="${instance.getValue(context, presentationEntry)}"></c:set>
                                    <xbrl:field reportForm="${reportForm}" presentationEntry="${presentationEntry}"
                                            reportMetamodel="${formMetamodel}" valueEntry="${valueEntry}"
                                            totalInput="${formMetamodel.isTotalPresentation(presentationEntry)}"/>
                                </c:if>
                            </td>
                            <c:set var="count" value="${count + 1}" scope="page"/>
                        </c:if>
                    </c:forEach>
                </c:if>
            </tr>
        </c:forEach>
    </tbody>
</table>