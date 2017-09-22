package com.nortal.xbrl.view.service;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.metamodel.TransformResult;
import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.view.entity.Report;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface XbrlService {

    XbrlInstance read(byte[] byteArray);

    TransformResult transform(XbrlInstance source, XbrlInstance target);

    String getXbrlFileContent(XbrlInstance instance);

    XbrlInstance getPopulatedXbrlInstance(Report report);

    List<XbrlError> validate(Report report, XbrlInstance instance,
                             ReportingFormType reportingFormType, Locale locale);

    XbrlInstance calculate(ReportingFormMetamodel report, XbrlInstance xbrlInstance);

    byte[] write(XbrlInstance xbrlInstance);

    XbrlInstance createInstanceModel(Report report);

    List<XbrlContext> getFormContexts(Report report, ReportingFormType reportingFormType);

    List<XbrlContext> getVisibleFormContexts(Report report, ReportingFormType reportingFormType);

    boolean reportingFormContainsData(Report report, XbrlInstance xbrlInstance, ReportingFormType reportingFormType);

    ReportingFormMetamodel getReportingFormMetamodel(String code);

    List<ReportingFormMetamodel> getReportingFormMetamodels(List<ReportingFormType> selectedForms);

}
