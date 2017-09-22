<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>

<%@ attribute name="reportForm" type="com.nortal.xbrl.view.model.ReportForm" required="true"%>
<%@ attribute name="context" type="com.nortal.xbrl.metamodel.XbrlContext" required="true"%>
<%@ attribute name="presentationEntry" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="reportMetamodel" type="com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel" required="true"%>
<%@ attribute name="lang" type="java.lang.String" required="true" %>

<c:set var="dimension" value="${reportMetamodel.getDimension(presentationEntry)}"></c:set>

<c:set var="table" value="${presentationEntry.children.get(0)}"></c:set>
<c:set var="tableDimension" value="${dimension.getChild(table)}"></c:set>

<c:set var="lineItems" value="${presentationEntry.children.get(1)}"></c:set>
<c:set var="lineItemsDimension" value="${dimension.getChild(table)}"></c:set>

<c:set var="axis" value="${table.children.get(0)}"></c:set>
<c:set var="axisDimension" value="${tableDimension.getChild(axis)}"></c:set>

<table class="table table-hover">
    <thead>
        <tr>
            <th colspan="2"></th>
            <xbrl:hypercubeAxis axis="${axis}" dimension="${axisDimension}" lang="${lang}"/>
        </tr>
    </thead>
    <tbody>
        <xbrl:hypercubeMember reportForm="${reportForm}" context="${context}" axis="${axis}"
                axisDimension="${axisDimension}" lineItem="${lineItems}" reportMetamodel="${reportMetamodel}" lang="${lang}"/>
    </tbody>
</table>
