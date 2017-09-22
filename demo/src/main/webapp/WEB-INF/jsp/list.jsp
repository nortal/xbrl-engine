<%@include file="_header.jsp" %>
    <div class="container mt-2">
        <c:if test="${not empty xbrlErrors}">
            <div class="alert alert-danger">
                <p><spring:message code="collapse_header.errors_found"/> <a href="#xbrlErrors" data-toggle="collapse" class="btn btn-secondary"><spring:message code="button.show"/></a></p>
                <div class="collapse" id="xbrlErrors">
                    <ul class="list-unstyled">
                        <c:forEach items="${xbrlErrors}" var="error">
                            <li>
                                <fmt:message key="${error.code}">
                                    <c:url var="errorFormUrl" value="/${reportId}/${error.arguments[0]}"/>
                                    <c:forEach items="${error.arguments}" var="paramValue" varStatus="loop">
                                        <c:if test="${loop.index > 0}">
                                            <fmt:param value="${paramValue}" />
                                        </c:if>
                                    </c:forEach>
                                    <fmt:param value="${errorFormUrl}" />
                                </fmt:message>
                            </li>
                        </c:forEach>
                    </ul>
                </div>
            </div>
        </c:if>

        <div class="card">
            <div class="card-header">
                <span class="fa fa-table"></span>
                <spring:message code="form.financial.statements.title"/>
            </div>
            <div class="card-body">
                <table class="table table-hover">
                    <thead>
                    <tr>
                        <th><spring:message code="form.financial.statements.number"/></th>
                        <th><spring:message code="form.financial.statements.reporting.form"/></th>
                        <th><spring:message code="forms.form.data.entered"/></th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${!reportForm.report.selectedForms.isEmpty()}">
                            <c:set var="no" value="${0}" />

                            <c:forEach items="${reportForm.report.selectedForms}" var="form">
                                <c:set var="no" value="${no + 1}" />
                                <tr>
                                    <td>${no}</td>
                                    <td>
                                        <strong>
                                            [${form.code}] ${reportForm.getReportingFormName(form.code, lang)}
                                        </strong>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${form.containsData()}">
                                                <strong class="text-success">
                                                    <spring:message code="forms.form.data.entered.yes"/>
                                                </strong>
                                            </c:when>
                                            <c:otherwise>
                                                <strong class="text-danger">
                                                    <spring:message code="forms.form.data.entered.no"/>
                                                </strong>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="<c:url value="/${reportId}/${form.code}"/>" class="btn btn-info btn-sm">
                                            <spring:message code="button.open"/>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>

                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td colspan="5">
                                    <spring:message code="form.financial.statements.reporting.form.empty"/>
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
            <div class="card-footer text-right">
                <a href="<c:url value="/${reportId}/download"/>" class="btn btn-default">
                    <span class="fa fa-file"></span>
                    <spring:message code="button.download"/>
                </a>
                <a href="<c:url value="/${reportId}/validate"/>" class="btn btn-primary">
                    <span class="fa fa-check-circle"></span>
                    <spring:message code="button.validate"/>
                </a>
            </div>
        </div>
    </div>
<%@include file="_footer.jsp" %>