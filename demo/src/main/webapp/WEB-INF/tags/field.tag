<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<%@ attribute name="reportForm" type="com.nortal.xbrl.view.model.ReportForm" required="true"%>
<%@ attribute name="presentationEntry" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="reportMetamodel" type="com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel" required="true"%>
<%@ attribute name="valueEntry" type="com.nortal.xbrl.metamodel.XbrlValueEntry" required="true"%>
<%@ attribute name="totalInput" type="java.lang.Boolean" required="true"%>

<c:set var="collisionTooltip" value="" />
<c:forEach items="${reportForm.report.selectedForms}" var="form">
    <c:if test="${presentationEntry.ancestors.contains(form.code) && form.code != reportMetamodel.code}">
        <c:if test="${!collisionTooltip.isEmpty()}">
            <c:set var="collisionTooltip" value="${collisionTooltip}," />
        </c:if>
        <c:set var="collisionTooltip" value="${collisionTooltip} [${form.code}] ${reportForm.getReportingFormName(form.code, lang)}"/>
    </c:if>
</c:forEach>

<c:if test="${reportForm.report.multiplier == 'ONE'}">
    <c:set var="multiplierClass" value="one" />
</c:if>
<c:if test="${reportForm.report.multiplier == 'THOUSAND'}">
    <c:set var="multiplierClass" value="thousand" />
</c:if>

<span class="${collisionTooltip.isEmpty() ? '' : 'collision-field'}">
    <c:set var="collisionExists" value="${!collisionTooltip.isEmpty()}" />
    <spring:message code="field.appears.on.another.forms" var="fieldOnAnotherForms"/>
    <spring:message code="field.appears.on.other.forms" var="fieldOnOtherForms"/>
    <c:choose>
        <c:when test="${presentationEntry.type=='TEXT_BLOCK'}">
            <form:textarea path="values['${valueEntry.id}']"
                           cssClass="form-control textblock-field"
                           maxlength="2000"
                           data-html="true"
                           data-toggle="${collisionExists ? 'popover' : ''}"/>
        </c:when>
        <c:when test="${presentationEntry.type=='STRING'}">
            <form:input path="values['${valueEntry.id}']"
                        cssClass="form-control input-sm string-field"
                        data-html="true"
                        data-toggle="${collisionExists ? 'popover' : ''}"
                        data-content="${fieldOnAnotherForms}: ${collisionTooltip}" />
        </c:when>
        <c:when test="${presentationEntry.type=='DATE'}">
            <div class="input-group date input-group-date pull-right">
                <span class="input-group-addon">
                    <i class="fa fa-calendar"></i>
                </span>
                <form:input path="values['${valueEntry.id}']"
                            cssClass="form-control input-sm date-field"
                            data-html="true"
                            data-toggle="${collisionExists ? 'popover' : ''}"
                            data-content="${fieldOnAnotherForms}: ${collisionTooltip}" />
            </div>
        </c:when>
        <c:when test="${presentationEntry.type=='PER_SHARE'}">
            <form:input path="values['${valueEntry.id}']"
                        cssClass="form-control input-sm monetary-field pershare-field ${totalInput ? 'total_input' : ''}"
                        data-html="true"
                        data-toggle="${collisionExists ? 'popover' : ''}"
                        data-content="${fieldOnAnotherForms}: ${collisionTooltip}" />
            </div>
        </c:when>
        <c:otherwise>
            <form:input path="values['${valueEntry.id}']"
                        cssClass="form-control input-sm monetary-field monetary-field-${multiplierClass} ${totalInput ? 'total_input' : ''}"
                        data-html="true"
                        data-toggle="${collisionExists ? 'popover' : ''}"
                        data-content="${fieldOnAnotherForms}: ${collisionTooltip}" />
        </c:otherwise>
    </c:choose>
</span>


