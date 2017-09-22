package com.nortal.xbrl.view.service;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.model.StatementType;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.view.entity.Report;
import com.nortal.xbrl.view.entity.ReportField;

import java.util.List;

public interface ReportService {

    Report createReport();

    Report getReport(Long reportId);

    List<ReportField> getReportFields(Long reportId);

    Report saveReport(Report report);

    XbrlInstance saveReportFields(Report report, XbrlInstance xbrlInstance);

    List<StatementType> getListOfStatementTypes();

    StatementType getStatementTypeByCode(String code);

    List<ReportingFormType> getListOfReportingFormTypes();

    ReportingFormType getReportingFormTypeByCode(String code);

}
