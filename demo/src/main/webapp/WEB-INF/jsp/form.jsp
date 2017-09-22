<%@include file="_header.jsp" %>
    <c:set var="presentationEntries" value="${formMetamodel.getFlatPresentation()}" />
    <c:set var="hypercubeEntries" value="${formMetamodel.getHypercubePresentation()}" />

    <div class="container mt-2">
        <c:if test="${not empty message}">
            <div class="alert alert-success">
                <ul class="list-unstyled">
                    <li>${message}</li>
                </ul>
            </div>
        </c:if>

        <spring:bind path="reportForm">
            <c:if test="${status.error}">
                <div class="alert alert-danger">
                    <ul class="list-unstyled">
                        <spring:message code="xbrl.save.validate.errors.found"/>
                        <li>
                            <form:errors path="reportForm" delimiter="</li><li>" cssClass="error-message" />
                        </li>
                    </ul>
                </div>
            </c:if>
        </spring:bind>

        <form:form commandName="reportForm" method="post" servletRelativeAction="/${reportId}/${formMetamodel.code}/save">
            <div class="card">
                <div class="card-header">
                    <span class="fa fa-table"></span>
                    [${formMetamodel.code}] ${formMetamodel.name[lang]}
                    <span class="pull-right">
                        <c:if test="${reportForm.report.multiplier == 'ONE'}">
                            <spring:message code="amounts.eur"/>
                        </c:if>
                        <c:if test="${reportForm.report.multiplier == 'THOUSAND'}">
                            <spring:message code="amounts.eur.thousands"/>
                        </c:if>
                    </span>
                </div>
                <div class="card-body">
                    <c:if test="${not formMetamodel.hasOnlyTextFields() || not empty hypercubeEntries}">
                        <p><spring:message code="form.header.explanatory.note"/></p>
                    </c:if>

                    <c:if test="${!empty presentationEntries}">
                        <xbrl:flatForm reportForm="${reportForm}" formMetamodel="${formMetamodel}" contexts="${contexts}" lang="${lang}"/>
                    </c:if>

                    <c:forEach items="${hypercubeEntries}" var="hypercubeEntry">
                        <c:forEach items="${contexts}" var="context">
                            <c:if test="${not context.hasExplicitMember()}">
                                <xbrl:hypercube reportForm="${reportForm}" context="${context}" reportMetamodel="${formMetamodel}" presentationEntry="${hypercubeEntry}" lang="${lang}"/>
                            </c:if>
                        </c:forEach>
                    </c:forEach>
                </div>
                <div class="card-footer text-right">
                    <button type="submit" name="save" class="btn btn-primary">
                        <span class="fa fa-save"></span>
                        <spring:message code="button.save.close"/>
                    </button>
                    <c:if test="${not empty formMetamodel.calculation}">
                        <button type="submit" name="recalculate" class="btn btn-default">
                            <span class="fa fa-refresh"></span>
                            <spring:message code="button.recalculate"/>
                        </button>
                    </c:if>
                </div>
            </div>
        </form:form>
    </div>
<%@include file="_footer.jsp" %>