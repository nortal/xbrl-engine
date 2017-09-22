package com.nortal.xbrl.view.service.impl;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.model.StatementType;
import com.nortal.xbrl.metamodel.XbrlFieldValue;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.XbrlEngine;
import com.nortal.xbrl.view.entity.Report;
import com.nortal.xbrl.view.entity.ReportField;
import com.nortal.xbrl.view.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReportServiceImpl implements ReportService {

    private static final List<StatementType> statementTypes;

    // TODO these method should be actually retrieve from the DB
    static {
        StatementType mc = new StatementType();
        mc.setId(1L);
        mc.setCode("MC");
        mc.setName("Management commentary");
        mc.setMandatory(false);
        mc.setComparativePeriod(0);

        StatementType fp = new StatementType();
        fp.setId(2L);
        fp.setCode("FP");
        fp.setName("Financial position");
        fp.setMandatory(true);
        fp.setComparativePeriod(1);

        StatementType pl = new StatementType();
        pl.setId(3L);
        pl.setCode("PL");
        pl.setName("Income statement");
        pl.setMandatory(true);
        pl.setComparativePeriod(1);

        StatementType cf = new StatementType();
        cf.setId(4L);
        cf.setCode("CF");
        cf.setName("Cash flows");
        cf.setMandatory(false);
        cf.setComparativePeriod(1);

        StatementType eq = new StatementType();
        eq.setId(5L);
        eq.setCode("EQ");
        eq.setName("Changes in equity");
        eq.setMandatory(true);
        eq.setComparativePeriod(1);

        statementTypes = Arrays.asList(mc, fp, pl, cf, eq);
    }

    @Autowired
    private XbrlEngine xbrlEngine;

    @Override
    public Report createReport() {
        Report report = new Report();
        report.setId(newId());
        report.setPeriodStartDate(getThisYearStart());
        report.setPeriodEndDate(getThisYearEnd());
        report.setPrecedingPeriodStartDate(getLastYearStart());
        report.setPrecedingPeriodEndDate(getLastYearEnd());

        reportStorage.put(report.getId(), report);

        return report;
    }

    @Override
    public Report getReport(Long reportId) {
        return reportStorage.get(reportId);
    }

    @Override
    public List<ReportField> getReportFields(Long reportId) {
        if (!reportFieldStorage.containsKey(reportId)) {
            return null;
        }

        return new ArrayList<>(reportFieldStorage.get(reportId).values());
    }

    @Override
    public Report saveReport(Report report) {
        reportStorage.put(report.getId(), report);

        return report;
    }

    @Override
    public XbrlInstance saveReportFields(Report report, XbrlInstance xbrlInstance) {
        List<XbrlFieldValue> xbrlFields = xbrlEngine.getBuilder().createBuilder().build(xbrlInstance);
        storeValues(report.getId(), xbrlFields);
        xbrlInstance.resetValueEntriesDirtyFlags();

        return xbrlInstance;
    }

    @Override
    public List<StatementType> getListOfStatementTypes() {
        return statementTypes;
    }

    @Override
    public StatementType getStatementTypeByCode(String code) {
        return getListOfStatementTypes()
                .stream()
                .filter(t -> t.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Statement type not found by code"));
    }

    @Override
    public List<ReportingFormType> getListOfReportingFormTypes() {
        // TODO these method should actually retrieve data from the DB

        ReportingFormType form110000 = new ReportingFormType();
        form110000.setId(1L);
        form110000.setCode("110000");
        form110000.setName("Disclosure of general information about financial statements");
        form110000.setStatementType(getStatementTypeByCode("MC"));

        ReportingFormType form210000 = new ReportingFormType();
        form210000.setId(2L);
        form210000.setCode("210000");
        form210000.setName("Statement of financial position");
        form210000.setStatementType(getStatementTypeByCode("FP"));

        ReportingFormType form220000 = new ReportingFormType();
        form220000.setId(3L);
        form220000.setCode("220000");
        form220000.setName("Statement of financial position");
        form220000.setStatementType(getStatementTypeByCode("FP"));

        ReportingFormType form310000 = new ReportingFormType();
        form310000.setId(4L);
        form310000.setCode("310000");
        form310000.setName("Profit or loss");
        form310000.setStatementType(getStatementTypeByCode("PL"));

        ReportingFormType form320000 = new ReportingFormType();
        form320000.setId(5L);
        form320000.setCode("320000");
        form320000.setName("Profit or loss");
        form320000.setStatementType(getStatementTypeByCode("PL"));

        ReportingFormType form410000 = new ReportingFormType();
        form410000.setId(6L);
        form410000.setCode("410000");
        form410000.setName("Statement of comprehensive income");
        form410000.setStatementType(getStatementTypeByCode("PL"));

        ReportingFormType form420000 = new ReportingFormType();
        form420000.setId(7L);
        form420000.setCode("420000");
        form420000.setName("Statement of comprehensive income");
        form420000.setStatementType(getStatementTypeByCode("PL"));

        ReportingFormType form610000 = new ReportingFormType();
        form610000.setId(8L);
        form610000.setCode("610000");
        form610000.setName("Statement of changes in equity");
        form610000.setStatementType(getStatementTypeByCode("EQ"));

        ReportingFormType form800400 = new ReportingFormType();
        form800400.setId(9L);
        form800400.setCode("800400");
        form800400.setName("Statement of changes in equity");
        form800400.setStatementType(getStatementTypeByCode("EQ"));

        ReportingFormType form800500 = new ReportingFormType();
        form800500.setId(10L);
        form800500.setCode("800500");
        form800500.setName("Disclosure of notes and other explanatory information");
        form800500.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form800600 = new ReportingFormType();
        form800600.setId(11L);
        form800600.setCode("800600");
        form800600.setName("Disclosure of significant accounting policies");
        form800600.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form815000 = new ReportingFormType();
        form815000.setId(12L);
        form815000.setCode("815000");
        form815000.setName("Disclosure of events after reporting period");
        form815000.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form822100 = new ReportingFormType();
        form822100.setId(13L);
        form822100.setCode("822100");
        form822100.setName("Disclosure of property, plant and equipment");
        form822100.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form831110 = new ReportingFormType();
        form831110.setId(14L);
        form831110.setCode("831110");
        form831110.setName("Disclosure of revenue");
        form831110.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form831710 = new ReportingFormType();
        form831710.setId(15L);
        form831710.setCode("831710");
        form831710.setName("Disclosure of recognised revenue from construction contracts");
        form831710.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form832600 = new ReportingFormType();
        form832600.setId(16L);
        form832600.setCode("832600");
        form832600.setName("Disclosure of leases");
        form832600.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form834480 = new ReportingFormType();
        form834480.setId(17L);
        form834480.setCode("834480");
        form834480.setName("Disclosure of employee benefits");
        form834480.setStatementType(getStatementTypeByCode("CF"));

        ReportingFormType form835110 = new ReportingFormType();
        form835110.setId(18L);
        form835110.setCode("835110");
        form835110.setName("Disclosure of income tax");
        form835110.setStatementType(getStatementTypeByCode("CF"));

        return Arrays.asList(form110000, form210000, form220000, form310000, form320000, form410000, form420000,
                form610000, form800400, form800500, form800600, form815000, form822100, form831110, form831710,
                form832600, form834480, form835110);
    }

    @Override
    public ReportingFormType getReportingFormTypeByCode(String code) {
        return getListOfReportingFormTypes()
                .stream()
                .filter(t -> t.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reporting form type not found by code"));
    }

    /**
     * Demo code for in memory storage, you will have to implement real DB storage if you want to store fill reports.
     */

    private Map<Long, Report> reportStorage = new HashMap<>();
    private Map<Long, Map<Long, ReportField>> reportFieldStorage = new HashMap<>();

    private void storeValues(Long reportId, List<XbrlFieldValue> xbrlFields) {
        Map<String, ReportField> fieldsToStore = getFieldMap(reportId, xbrlFields);
        populateFieldIds(reportId,  fieldsToStore);

        if (!reportFieldStorage.containsKey(reportId)) {
            reportFieldStorage.put(reportId, new HashMap<>());
        }

        for (ReportField field : fieldsToStore.values()) {
            if (field.getValueString() == null && field.getId() != null) {
                reportFieldStorage.get(reportId).remove(field.getId());
            } else if (field.getValueString() != null) {
                reportFieldStorage.get(reportId).put(field.getId(), field);
            }
        }
    }

    private Map<String, ReportField> getFieldMap(Long reportId, List<XbrlFieldValue> xbrlFields) {
        Map<String, ReportField> fieldsToStore = new HashMap<>();
        for (XbrlFieldValue xbrlField : xbrlFields) {
            ReportField formField = buildField(reportId, xbrlField);
            fieldsToStore.put(formField.getKey(), formField);
        }
        return fieldsToStore;
    }

    private void populateFieldIds(Long reportId, Map<String, ReportField> fieldsToStore) {
        List<ReportField> storedFields = getReportFields(reportId);

        // No fields stored yet
        if (storedFields == null) {
            return;
        }

        for (ReportField storedField : storedFields) {
            String storedFieldKey = storedField.getKey();

            if (fieldsToStore.get(storedFieldKey) != null) {
                storedField.setValue(fieldsToStore.get(storedFieldKey).getValue());
                fieldsToStore.put(storedFieldKey, storedField);
            }
        }
    }

    private ReportField buildField(Long reportId, XbrlFieldValue xbrlField) {
        ReportField formField = new ReportField();

        formField.setId(newId());
        formField.setType(xbrlField.getValueType().name());
        formField.setReport(getReport(reportId));
        formField.setValue(xbrlField.getValue());
        formField.setName(xbrlField.getName());
        formField.setPeriodStartDate(xbrlField.getStartDate());
        formField.setPeriodEndDate(xbrlField.getEndDate());

        return formField;
    }

    private Date getThisYearStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2017);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    private Date getThisYearEnd() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisYearStart());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_YEAR, 31);
        return cal.getTime();
    }

    private Date getLastYearStart() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    private Date getLastYearEnd() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getLastYearStart());
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DAY_OF_YEAR, 31);
        return cal.getTime();
    }

    private Long newId() {
        return Math.abs(new Random().nextLong());
    }

}
