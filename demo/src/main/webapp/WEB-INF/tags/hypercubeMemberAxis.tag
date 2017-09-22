<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<%@ attribute name="reportForm" type="com.nortal.xbrl.view.model.ReportForm" required="true"%>
<%@ attribute name="context" type="com.nortal.xbrl.metamodel.XbrlContext" required="true"%>
<%@ attribute name="axis" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="lineItem" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="reportMetamodel" type="com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel" required="true"%>

<c:set var="instance" value="${reportForm.xbrlInstance}"></c:set>

<c:forEach var="child" items="${axis.children}">
    <xbrl:hypercubeMemberAxis reportForm="${reportForm}" context="${context}" axis="${child}" lineItem="${lineItem}" reportMetamodel="${reportMetamodel}"/>
</c:forEach>
<c:choose>
    <c:when test="${lineItem['abstract'] && axis.type != 'AXIS'}">
        <td></td>
    </c:when>
    <c:when test="${axis.type != 'AXIS'}">
        <td>
            <c:set var="dimension" value="${reportMetamodel.getDimension(axis)}"></c:set>
            <c:set var="valueEntry" value="${instance.getValue(context, lineItem, dimension)}"></c:set>          
            <c:set var="totalInput" value="${reportMetamodel.isTotalPresentation(lineItem) || dimension.arcRole == 'DIMENSION_DOMAIN'}" />
            <xbrl:field reportForm="${reportForm}" presentationEntry="${lineItem}" reportMetamodel="${reportMetamodel}" valueEntry="${valueEntry}" totalInput="${totalInput}"/>
        </td>
    </c:when>
</c:choose>