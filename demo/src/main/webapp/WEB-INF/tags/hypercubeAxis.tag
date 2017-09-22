<%@ tag pageEncoding="UTF-8"%>
<%@ taglib prefix="xbrl" uri="http://xbrl-engine-fragment" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<%@ attribute name="axis" type="com.nortal.xbrl.metamodel.meta.PresentationEntry" required="true"%>
<%@ attribute name="dimension" type="com.nortal.xbrl.metamodel.meta.DimensionEntry" required="true"%>
<%@ attribute name="lang" type="java.lang.String" required="true" %>

<c:if test="${!empty axis.children}">
    <c:forEach var="childAxis" items="${axis.children}">
        <xbrl:hypercubeAxis axis="${childAxis}" dimension="${dimension.getChild(childAxis)}" lang="${lang}"/>
    </c:forEach>
</c:if>

<c:if test="${dimension.arcRole == 'DOMAIN_MEMBER' || dimension.arcRole == 'DIMENSION_DOMAIN'}">
    <th class="text-center col-total">
        ${axis.getPreferredLabel(lang)}
    </th>
</c:if>
