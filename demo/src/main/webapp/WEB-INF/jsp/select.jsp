<%@include file="_header.jsp" %>
    <div class="container mt-2">
        <spring:bind path="composeReportForm">
            <c:if test="${status.error}">
                <div class="alert alert-danger">
                    <ul class="list-unstyled">
                        <li>
                            <form:errors path="composeReportForm" delimiter="</li><li>" cssClass="error-message" />
                        </li>
                    </ul>
                </div>
            </c:if>
        </spring:bind>

        <form:form commandName="composeReportForm" method="post" servletRelativeAction="/${reportId}/save" enctype="multipart/form-data">
            <div class="card">
                <div class="card-header">
                    <span class="fa fa-table"></span>
                    <spring:message code="forms.selection"/>
                    <span class="pull-right">
                        <spring:message code="forms.form.data.upload.accounting.period"/> <strong>${composeReportForm.accountingYear}</strong>
                    </span>
                </div>
                <div class="card-body">
                    <p>
                        <spring:message code="forms.description"/>
                    </p>
                    <div class="form-group row">
                        <label class="col-sm-4 col-form-label"><spring:message code="forms.amounts.presentation"/></label>
                        <div class="col-sm-8">
                            <form:select path="report.multiplier" class="form-control">
                                <spring:message code="amounts.eur" var="eurLabel"/>
                                <spring:message code="amounts.eur.thousands" var="eurThousandsLabel"/>
                                <form:option value="ONE" label="${eurLabel}" />
                                <form:option value="THOUSAND" label="${eurThousandsLabel}" />
                            </form:select>
                        </div>
                    </div>
                    <p>
                        <spring:message code="forms.selection.description"/>
                    </p>
                    <table class="table table-hover">
                        <thead>
                        <tr>
                            <th><spring:message code="form.financial.statements.number"/> </th>
                            <th><spring:message code="form.financial.statements.reporting.form"/></th>
                            <th></th>
                            <th><spring:message code="forms.form.data.entered"/> </th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:choose>
                            <c:when test="${not empty composeReportForm.statementTypes}">
                                <c:forEach items="${composeReportForm.statementTypes}" var="statementType" varStatus="number">
                                    <c:set var="availableForms" value="${composeReportForm.getAvailableForms(statementType)}"/>
                                    <c:set var="selectedForms" value="${composeReportForm.report.selectedForms}"/>
                                    <c:if test="${not empty availableForms}">
                                        <tr>
                                            <td>${number.index + 1}</td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${availableForms.size() > 1}">
                                                        <form:select path="selectedFormCodes['${number.index}']"
                                                                     class="form-control input-sm width-auto"
                                                                     multiple="false"
                                                                     onchange="updateHasValueStatus(this);">
                                                            <c:if test="${not statementType.mandatory}">
                                                                <form:option value="" label="" data-hasValues="false" />
                                                            </c:if>
                                                            <c:forEach items="${availableForms}" var="form">
                                                                <c:choose>
                                                                    <c:when test="${composeReportForm.getIsFormSelected(form)}">
                                                                        <form:option selected="true"
                                                                                     value="${form.code}"
                                                                                     label="[${form.getCode()}] ${composeReportForm.getReportingFormName(form.getCode(), lang)}"
                                                                                     data-hasValues="${form.containsData()}" />
                                                                        <c:set var="formHasData" value="${form.containsData()}"/>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <form:option value="${form.code}"
                                                                                     label="[${form.getCode()}] ${composeReportForm.getReportingFormName(form.getCode(), lang)}"
                                                                                     data-hasValues="${form.containsData()}" />
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </c:forEach>
                                                        </form:select>
                                                    </c:when>
                                                    <c:when test="${statementType.mandatory}">
                                                        <c:set var="formHasData" value="${availableForms[0].containsData()}"/>
                                                        <form:hidden path="selectedFormCodes['${number.index}']" value="${availableForms[0].code}"/>
                                                        <strong>[${availableForms[0].code}] ${composeReportForm.getReportingFormName(availableForms[0].code, lang)}</strong>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:set var="formHasData" value="${availableForms[0].containsData()}"/>
                                                        <strong>[${availableForms[0].code}] ${composeReportForm.getReportingFormName(availableForms[0].code, lang)}</strong>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${statementType.mandatory}">
                                                        <c:choose>
                                                            <c:when test="${availableForms.size() > 1}">
                                                                <spring:message code="forms.selection.choose.one"/>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <spring:message code="forms.selection.required"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:choose>
                                                            <c:when test="${availableForms.size() == 1}">
                                                                <div class="checkbox">
                                                                    <c:choose>
                                                                        <c:when test="${composeReportForm.getIsFormSelected(availableForms[0])}">
                                                                            <form:checkbox path="selectedFormCodes['${number.index}']" value="${availableForms[0].code}" checked="checked"/>
                                                                        </c:when>
                                                                        <c:otherwise>
                                                                            <form:checkbox path="selectedFormCodes['${number.index}']" value="${availableForms[0].code}" />
                                                                        </c:otherwise>
                                                                    </c:choose>
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <spring:message code="forms.selection.choose.optional"/>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty selectedForms}">
                                                        <span class="status-no-value">
                                                            <spring:message code="forms.form.data.entered.no"/>
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <c:choose>
                                                            <c:when test="${formHasData}">
                                                                <strong class="status-has-value text-success d-inline">
                                                                    <spring:message code="forms.form.data.entered.yes"/>
                                                                </strong>
                                                                <strong class="status-no-value text-danger d-none">
                                                                    <spring:message code="forms.form.data.entered.no"/>
                                                                </strong>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <strong class="status-has-value text-success d-none">
                                                                    <spring:message code="forms.form.data.entered.yes"/>
                                                                </strong>
                                                                <strong class="status-no-value text-danger d-inline">
                                                                    <spring:message code="forms.form.data.entered.no"/>
                                                                </strong>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="4" bgcolor="#FBF0DB" align="center"><spring:message code="forms.selection.unavailable"/></td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>

                    <div id="xbrlUploadBox" data-children=".item">
                        <div class="item">
                            <a data-toggle="collapse" data-parent="#xbrlUploadBox" href="#xbrlUploadBoxPanel" aria-expanded="true" aria-controls="xbrlUploadBoxPanel" class="btn btn-secondary">
                                <spring:message code="forms.form.data.upload.title"/>
                            </a>
                            <div id="xbrlUploadBoxPanel" class="collapse" role="tabpanel">
                                <p class="mt-2">
                                    <spring:message code="forms.form.data.upload.description"/>
                                </p>
                                <div class="mt-2 mb-2">
                                    <label class="custom-file">
                                        <input type="file" id="file" name="instanceFile" class="custom-file-input" data-toggle="custom-file" data-target="#upload-xbrl">
                                        <span id="upload-xbrl" class="custom-file-control custom-file-name" data-content="<spring:message code="forms.form.data.upload.file.selection"/>"></span>
                                    </label>
                                    <button type="submit" name="upload" class="btn btn-primary"><spring:message code="button.upload"/></button>
                                </div>
                                <p class="alert alert-warning">
                                    <spring:message code="forms.form.data.upload.warning"/>
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="card-footer text-right">
                    <button type="submit" name="save" class="btn btn-primary">
                        <span class="fa fa-save"></span>
                        <spring:message code="button.save"/>
                    </button>
                </div>
            </div>
        </form:form>
    </div>
<%@include file="_footer.jsp" %>