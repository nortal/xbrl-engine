package com.nortal.xbrl.view.service.impl;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.metamodel.TransformResult;
import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlFieldValue;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.XbrlBuilder;
import com.nortal.xbrl.XbrlEngine;
import com.nortal.xbrl.view.entity.Report;
import com.nortal.xbrl.view.entity.ReportField;
import com.nortal.xbrl.view.service.ReportService;
import com.nortal.xbrl.view.service.XbrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class XbrlServiceImpl implements XbrlService {

    public static final String METAMODEL_XML = "metamodel.xml";

    private Map<String, ReportingFormMetamodel> reportingFormMetamodels = new HashMap<String, ReportingFormMetamodel>();

    @Autowired
    private XbrlEngine xbrlEngine;

    @Autowired
    private ReportService reportService;

    @PostConstruct
    public void initialize() {
        ClassPathResource cpr = new ClassPathResource(METAMODEL_XML);

        try {
            reportingFormMetamodels = getXbrlEngine().getMapper().getReportingFormMetamodels(cpr.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public XbrlInstance read(byte[] byteArray) {
        return getXbrlEngine().getReader().read(new ByteArrayInputStream(byteArray));
    }

    @Override
    public TransformResult transform(XbrlInstance source, XbrlInstance target) {
        return getXbrlEngine().getTransformer().transform(source, target);
    }

    @Override
    public String getXbrlFileContent(XbrlInstance instanceModel) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        getXbrlEngine().getWriter().write(instanceModel, outputStream);

        return outputStream.toString();
    }

    @Override
    public XbrlInstance getPopulatedXbrlInstance(Report report) {
        XbrlInstance xbrlInstance = createInstanceModel(report);
        List<XbrlFieldValue> values = loadValues(report.getId());
        return getXbrlEngine().getBuilder().createBuilder().populate(xbrlInstance, values);
    }

    private String getLanguage(Locale locale) {
        return locale.getLanguage();
    }

    @Override
    public List<XbrlError> validate(Report report, XbrlInstance xbrlInstance,
                                    ReportingFormType reportingFormType, Locale locale) {
        ReportingFormMetamodel metamodel = getReportingFormMetamodel(reportingFormType.getCode());
        List<XbrlContext> contexts = getFormContexts(report, reportingFormType);
        String lang = getLanguage(locale);
        return getXbrlEngine().getValidator().validate(metamodel, xbrlInstance, contexts, lang);
    }

    @Override
    public XbrlInstance calculate(ReportingFormMetamodel report, XbrlInstance xbrlInstance) {
        return getXbrlEngine().getCalculator().calculate(report, xbrlInstance);
    }

    @Override
    public byte[] write(XbrlInstance xbrlInstance) {
        return getXbrlEngine().getWriter().write(xbrlInstance);
    }

    @Override
    public XbrlInstance createInstanceModel(Report report) {
        List<ReportingFormType> selectedForms = report.getSelectedForms();
        if (selectedForms == null || selectedForms.isEmpty()) {
            throw new RuntimeException("Form list can not be null or empty");
        }

        Map<String, XbrlBuilder.PeriodRange> periodRangeMap = new HashMap<>();
        List<ReportingFormMetamodel> reportingForms = new ArrayList<>();

        for (ReportingFormType form : selectedForms) {
            ReportingFormMetamodel formMetamodel = getReportingFormMetamodel(form.getCode());
            reportingForms.add(formMetamodel);
            XbrlBuilder.PeriodRange periodRange = getXbrlEngine().getBuilder().getPeriodRange(form);
            periodRangeMap.put(form.getCode(), periodRange);
        }

        return getXbrlEngine().getBuilder()
                .createBuilder()
                .setReportingForms(reportingForms)
                .setPeriodRanges(periodRangeMap)
                .setPeriodStart(report.getPeriodStartDate())
                .setPeriodEnd(report.getPeriodEndDate())
                .setPreviousPeriodStart(report.getPrecedingPeriodStartDate())
                .setPreviousPeriodEnd(report.getPrecedingPeriodEndDate())
                .setMultiplier(report.getMultiplier())
                .build();
    }

    @Override
    public List<XbrlContext> getVisibleFormContexts(Report report, ReportingFormType reportingFormType) {
        XbrlBuilder.PeriodRange periodRange = getXbrlEngine().getBuilder().getPeriodRange(reportingFormType);
        return getXbrlEngine().getBuilder().getVisibleFormContexts(periodRange, report.getPeriodStartDate(), report.getPeriodEndDate(),
                report.getPrecedingPeriodStartDate(), report.getPrecedingPeriodEndDate());
    }

    @Override
    public List<XbrlContext> getFormContexts(Report report, ReportingFormType reportingFormType) {
        XbrlBuilder.PeriodRange periodRange = getXbrlEngine().getBuilder().getPeriodRange(reportingFormType);
        return getXbrlEngine().getBuilder().getFormContexts(periodRange, report.getPeriodStartDate(), report.getPeriodEndDate(),
                report.getPrecedingPeriodStartDate(), report.getPrecedingPeriodEndDate());
    }

    @Override
    public boolean reportingFormContainsData(Report report, XbrlInstance xbrlInstance, ReportingFormType reportingFormType) {
        ReportingFormMetamodel metamodel = getReportingFormMetamodel(reportingFormType.getCode());
        List<XbrlContext> contexts = getFormContexts(report, reportingFormType);
        return getXbrlEngine().getValidator().formContainsData(xbrlInstance, contexts, metamodel);
    }

    @Override
    public ReportingFormMetamodel getReportingFormMetamodel(String code) {
        ReportingFormMetamodel model = reportingFormMetamodels.get(code);

        if (model == null) {
            throw new IllegalArgumentException("Unknown report code: " + code);
        }

        return model;
    }

    @Override
    public List<ReportingFormMetamodel> getReportingFormMetamodels(List<ReportingFormType> selectedForms) {
        List<ReportingFormMetamodel> metamodels = new ArrayList<ReportingFormMetamodel>();
        for(ReportingFormType form : selectedForms) {
            ReportingFormMetamodel metamodel = getReportingFormMetamodel(form.getCode());
            metamodels.add(metamodel);
        }
        return metamodels;
    }

    private List<XbrlFieldValue> loadValues(Long annualReportId) {
        List<XbrlFieldValue> xbrlFields = new ArrayList<>();
        List<ReportField> formFields = reportService.getReportFields(annualReportId);
        if (formFields != null) {
            for (ReportField formField : formFields) {
                xbrlFields.add(buildXbrlField(formField));
            }
        }
        return xbrlFields;
    }

    private XbrlFieldValue buildXbrlField(ReportField formField) {
        return new XbrlFieldValue.Builder()
                .setName(formField.getName())
                .setStartDate(formField.getPeriodStartDate())
                .setEndDate(formField.getPeriodEndDate())
                .setValue(formField.getValueString())
                .setValueType(XbrlFieldValue.ValueType.valueOf(formField.getType()))
                .build();
    }

    public XbrlEngine getXbrlEngine() {
        return xbrlEngine;
    }

    public ReportService getReportService() {
        return reportService;
    }

}
