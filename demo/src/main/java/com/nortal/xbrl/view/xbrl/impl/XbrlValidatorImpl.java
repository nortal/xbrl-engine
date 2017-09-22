package com.nortal.xbrl.view.xbrl.impl;

import com.nortal.xbrl.impl.XbrlBuilderImpl;
import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.CalculationEntry;
import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

@Slf4j
public class XbrlValidatorImpl extends com.nortal.xbrl.impl.XbrlValidatorImpl {

    private static final String IFRS_FULL_NAMESPACE = "http://xbrl.ifrs.org/taxonomy/2017-03-09/ifrs-full";
    private static final String IFRS_FULL_NAMESPACE_PREFIX = "ifrs-full";

    private static final String ASSETS_FIELD_NAME = "Assets";
    private static final String EQUITY_AND_LIABILITIES_FIELD_NAME = "EquityAndLiabilities";

    private static final String EQUITY_FIELD_NAME = "Equity";
    private static final String RESTATEDMEMBER_FIELD_NAME = "RestatedMember";
    private static final String CHANGESINEQUITY_FIELD_NAME = "ChangesInEquity";

    private static final String CASHANDCASHEQUIVALENTS_FIELD_NAME = "CashAndCashEquivalents";
    private static final String INCREASEDECREASEINCASHANDCASHEQUIVALENTS_FIELD_NAME = "IncreaseDecreaseInCashAndCashEquivalents";

    private static final String PROFITLOSS_FIELD_NAME = "ProfitLoss";
    private static final String OTHERCOMPREHENSIVEINCOME_FIELD_NAME = "OtherComprehensiveIncome";
    private static final String COMPREHENSIVEINCOME_FIELD_NAME = "ComprehensiveIncome";

    private static final String RULE_R02 = "xbrl.validate.error.r02";
    private static final String RULE_R03 = "xbrl.validate.error.r03";
    private static final String RULE_R04 = "xbrl.validate.error.r04";
    private static final String RULE_R05 = "xbrl.validate.error.r05";
    private static final String RULE_R06 = "xbrl.validate.error.r06";
    private static final String RULE_R07 = "xbrl.validate.error.r07";

    @Override
    public List<XbrlError> validate(ReportingFormMetamodel report, XbrlInstance instance, List<XbrlContext> contexts, String lang) {
        List<XbrlError> errors = new ArrayList<>();
        errors.addAll(super.validate(report, instance, contexts, lang));
        errors.addAll(validateCrossForm(report, instance, lang));
        return errors;
    }

    /**
     * Additional cross form validations example.
     *
     * @param report the reporting form
     * @param xbrlInstance the xbrl instance
     * @param lang the language code
     * @return validation errors
     */
    private List<XbrlError> validateCrossForm(ReportingFormMetamodel report, XbrlInstance xbrlInstance, String lang) {
        List<XbrlContext> contexts = filterPeriodContexts(xbrlInstance);

        List<XbrlError> errors = new ArrayList<XbrlError>();
        calculateBalance(xbrlInstance, report, errors, lang);
        calculateEquityChanges(xbrlInstance, errors, report, lang);
        calculateEquityEndPrecedingStartReporting(xbrlInstance, errors, report, contexts,lang);
        calculateEquityRestatedPlusChanges(xbrlInstance, errors, report, contexts, lang);
        calculateCashEquivalence(xbrlInstance, errors, report, contexts);
        calculateCashChange(xbrlInstance, errors, contexts, report);
        calculateProfitEquivalence(xbrlInstance, report, errors, lang);
        return errors;
    }

    /**
     * R01 Rule of balance in balance sheet
     */
    private void calculateBalance(XbrlInstance xbrlInstance, ReportingFormMetamodel report, List<XbrlError> errors, String lang) {
        for (XbrlContext context : xbrlInstance.getContexts().values()) {
            if (context.hasExplicitMember()) {
                continue;
            }
            calculateAccountingEquation(context, xbrlInstance, errors, report, lang);
        }
    }

    private boolean reportingFormContainsEntries(ReportingFormMetamodel report, CalculationEntry... calculationEntries) {
        for (CalculationEntry calculationEntry : calculationEntries) {
            if (report.getPresentation(calculationEntry) == null) {
                return false;
            }
        }

        return true;
    }

    protected void calculateAccountingEquation(XbrlContext context, XbrlInstance instance, List<XbrlError> errors,
            ReportingFormMetamodel report, String lang) {

        CalculationEntry assetsCalculationEntry = createCalculationEntry(ASSETS_FIELD_NAME);
        CalculationEntry equtyAndLiabilitiesCalculationEntry = createCalculationEntry(EQUITY_AND_LIABILITIES_FIELD_NAME);

        if (reportingFormContainsEntries(report, assetsCalculationEntry, equtyAndLiabilitiesCalculationEntry)) {
            XbrlValueEntry assets = instance.getValue(context, assetsCalculationEntry);
            XbrlValueEntry equityAndLiabilities = instance.getValue(context, equtyAndLiabilitiesCalculationEntry);

            if (assets != null && equityAndLiabilities != null && !assets.valueEquals(equityAndLiabilities)) {
                String errorCode = "xbrl.validate.error.template.equation";

                if (instance.getDisplayValue(assets) == null) {
                    errorCode += ".asset.null";
                } else if (instance.getDisplayValue(equityAndLiabilities) == null) {
                    errorCode += ".equity.null";
                }
                log.error("Assets do not equal to equity and liabilities");
                String name = report.getName().get(lang);
                String code = report.getCode();

                errors.add(new XbrlError.Builder().setCalculationEntryMetamodel(new CalculationEntry.Builder()
                        .addChild(assetsCalculationEntry)
                        .addChild(equtyAndLiabilitiesCalculationEntry).build())
                        .setContext(context)
                        .setBaseValue(assets.getValueAsBigDecimal())
                        .setCompareValue(equityAndLiabilities.getValueAsBigDecimal())
                        .setCode(errorCode)
                        .setArguments(code, name, instance.getDisplayValue(assets), instance.getDisplayValue(equityAndLiabilities), formatDate(context.getPeriodEndDate()))
                        .build());
            }
        }
    }

    /**
     * R02 Compliance between Equity components reported in Balance Sheet and in Changes in Equity
     */
    private void calculateEquityChanges(XbrlInstance xbrlInstance, List<XbrlError> errors, ReportingFormMetamodel report, String lang) {
        for (XbrlContext context : xbrlInstance.getContexts().values()) {
            if (context.hasExplicitMember()) {
                continue;
            }
            for (DimensionEntry dimension : report.getDimension()) {
                List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
                checkEquityDimensions(xbrlInstance, errors, dimensionDomains, context, report, lang);
            }
        }
    }

    private void checkEquityDimensions(XbrlInstance xbrlInstance, List<XbrlError> errors, List<DimensionEntry> dimensionDomains, XbrlContext context, ReportingFormMetamodel report, String lang) {
        CalculationEntry equityEntry = createCalculationEntry(EQUITY_FIELD_NAME);
        for (DimensionEntry dimensionDomain : dimensionDomains) {
            if (!dimensionDomain.getChildren().isEmpty()) {
                checkEquityDimensions(xbrlInstance, errors, dimensionDomain.getChildren(), context, report, lang);
            }
            XbrlValueEntry valueEntry = xbrlInstance.getValue(context, equityEntry, dimensionDomain);
            if (valueEntry != null) {
                String flatName = dimensionDomain.getName();
                flatName = flatName.substring(0, flatName.length() - "Member".length());
                CalculationEntry calculation = createCalculationEntry(flatName);
                XbrlValueEntry valueFlat = xbrlInstance.getValue(context, calculation);
                if (valueFlat != null && !valueFlat.valueEquals(valueEntry)) {
                    String label = report.getPresentation(dimensionDomain).getPreferredLabel(lang);
                    String contextEndDate = formatDate(context.getPeriodEndDate());

                    errors.add(new XbrlError.Builder().setValueEntryModel(valueFlat)
                        .setContext(context)
                        .setBaseValue(valueEntry.getValueAsBigDecimal())
                        .setCompareValue(valueFlat.getValueAsBigDecimal())
                        .setCode(RULE_R02)
                        .setArguments(null, label, contextEndDate, xbrlInstance.getDisplayValue(valueFlat), contextEndDate, label, xbrlInstance.getDisplayValue(valueEntry))
                        .build());
                }
            }
        }
    }

    /**
     * R03 Equity value for the end of preceding period equals to Equity value for the start of reporting period
     */
    private void calculateEquityEndPrecedingStartReporting(XbrlInstance xbrlInstance, List<XbrlError> errors, ReportingFormMetamodel report, List<XbrlContext> contexts, String lang) {
        XbrlContext precedingContext = contexts.get(0);
        XbrlContext reportingContext = contexts.get(1);
        Date precedingPeriodEnd = precedingContext.getPeriodEndDate();
        Date reportingPeriodStart = reportingContext.getPeriodStartDate();

        for (DimensionEntry dimension : report.getDimension()) {
            List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
            checkEquityDimensions(dimensionDomains, precedingPeriodEnd, reportingPeriodStart, xbrlInstance, errors, report, lang);
        }
    }

    private void checkEquityDimensions(List<DimensionEntry> dimensionDomains, Date precedingPeriodEnd, Date reportingPeriodStart,
            XbrlInstance xbrlInstance, List<XbrlError> errors, ReportingFormMetamodel report, String lang) {

        CalculationEntry equityEntry = createCalculationEntry(EQUITY_FIELD_NAME);
        for (DimensionEntry dimensionDomain : dimensionDomains) {
            if (!dimensionDomain.getChildren().isEmpty()) {
                checkEquityDimensions(dimensionDomain.getChildren(), precedingPeriodEnd, reportingPeriodStart, xbrlInstance, errors, report, lang);
            }

            XbrlContext precedingEndContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(precedingPeriodEnd).build();
            XbrlValueEntry precedingEndValue = xbrlInstance.getValue(precedingEndContext, equityEntry, dimensionDomain);

            XbrlContext reportingStartContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(reportingPeriodStart).build();
            XbrlValueEntry reportingStartValue = xbrlInstance.getValue(reportingStartContext, equityEntry, dimensionDomain);

            if (precedingEndValue != null && reportingStartValue != null && !reportingStartValue.valueEquals(precedingEndValue)) {
                String label = report.getPresentation(dimensionDomain).getPreferredLabel(lang);

                errors.add(new XbrlError.Builder().setValueEntryModel(precedingEndValue)
                    .setContext(precedingEndContext)
                    .setBaseValue(precedingEndValue.getValueAsBigDecimal())
                    .setCompareValue(reportingStartValue.getValueAsBigDecimal())
                    .setCode(RULE_R03)
                    .setArguments(null, label, formatDate(precedingPeriodEnd), xbrlInstance.getDisplayValue(precedingEndValue.getValueAsBigDecimal()),
                        formatDate(reportingPeriodStart), xbrlInstance.getDisplayValue(reportingStartValue.getValueAsBigDecimal()))
                    .build());
            }

        }
    }

    /**
     * R04 Equity value for the end of period equals to Restated Balance plus Equity change during period
     */
    private void calculateEquityRestatedPlusChanges(XbrlInstance xbrlInstance, List<XbrlError> errors, ReportingFormMetamodel report, List<XbrlContext> contexts, String lang) {
        for (DimensionEntry dimension : report.getDimension()) {
            List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
            checkEquityRestatedPlusChangesDimensions(dimensionDomains, xbrlInstance, errors, contexts, report, lang);
        }
    }

    private void checkEquityRestatedPlusChangesDimensions(List<DimensionEntry> dimensionDomains, XbrlInstance xbrlInstance, List<XbrlError> errors, List<XbrlContext> contexts, ReportingFormMetamodel report, String lang) {
        XbrlContext precedingContext = contexts.get(0);
        XbrlContext reportingContext = contexts.get(1);

        Date precedingPeriodEnd = precedingContext.getPeriodEndDate();
        Date precedingPeriodStart = precedingContext.getPeriodStartDate();

        Date reportingPeriodEnd = reportingContext.getPeriodEndDate();
        Date reportingPeriodStart = reportingContext.getPeriodStartDate();

        for (DimensionEntry dimensionDomain : dimensionDomains) {
            if (!dimensionDomain.getChildren().isEmpty()) {
                checkEquityRestatedPlusChangesDimensions(dimensionDomain.getChildren(), xbrlInstance, errors, contexts, report, lang);
            }
            calculateRestatedAndChangesSum(dimensionDomain, xbrlInstance, precedingPeriodStart, precedingPeriodEnd, errors, report, lang);
            calculateRestatedAndChangesSum(dimensionDomain, xbrlInstance, reportingPeriodStart, reportingPeriodEnd, errors, report, lang);
        }
    }

    private void calculateRestatedAndChangesSum(DimensionEntry dimensionDomain, XbrlInstance xbrlInstance, Date periodStart, Date periodEnd, List<XbrlError> errors, ReportingFormMetamodel report, String lang) {
        CalculationEntry restatedEntry = createCalculationEntry(RESTATEDMEMBER_FIELD_NAME);
        CalculationEntry changesInEquity = createCalculationEntry(CHANGESINEQUITY_FIELD_NAME);
        CalculationEntry equityEntry = createCalculationEntry(EQUITY_FIELD_NAME);

        XbrlContext periodEndContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(periodEnd).build();
        XbrlValueEntry precedingPeriodEndValue = xbrlInstance.getValue(periodEndContext, equityEntry, dimensionDomain);
        if (precedingPeriodEndValue != null) {
            XbrlContext periodStartContext = new XbrlContext.Builder().merge(periodEndContext).setInstant(periodStart).build();
            XbrlContext periodContext = new XbrlContext.Builder().merge(periodEndContext).setDateRange(periodStart, periodEnd).build();

            XbrlValueEntry restatedValue = xbrlInstance.getValue(periodStartContext, restatedEntry, dimensionDomain);
            XbrlValueEntry changesInEquityValue = xbrlInstance.getValue(periodContext, changesInEquity, dimensionDomain);

            if (restatedValue != null && changesInEquityValue != null) {
                BigDecimal restatedChangesSum = restatedValue.getValueAsBigDecimal().add(changesInEquityValue.getValueAsBigDecimal());
                if (!precedingPeriodEndValue.valueEquals(restatedChangesSum)) {
                    String label = report.getPresentation(dimensionDomain).getPreferredLabel(lang);

                    errors.add(new XbrlError.Builder().setValueEntryModel(precedingPeriodEndValue)
                            .setBaseValue(precedingPeriodEndValue.getValueAsBigDecimal())
                            .setCompareValue(restatedChangesSum)
                            .setCode(RULE_R04)
                            .setArguments(null, label, formatDate(periodEnd), xbrlInstance.getDisplayValue(precedingPeriodEndValue.getValueAsBigDecimal()),
                                    formatDate(periodStart), periodContext.getPeriodLabel(), xbrlInstance.getDisplayValue(restatedChangesSum))
                            .build());
                }
            }
        }
    }

    /**
     * R05 Cash for the end of preceding period equals to Cash for the start of reporting period
     */
    private void calculateCashEquivalence(XbrlInstance xbrlInstance, List<XbrlError> errors,
            ReportingFormMetamodel report, List<XbrlContext> contexts) {

        XbrlContext precedingContext = contexts.get(0);
        XbrlContext reportingContext = contexts.get(1);
        Date precedingPeriodEnd = precedingContext.getPeriodEndDate();
        Date reportingPeriodStart = reportingContext.getPeriodStartDate();

        CalculationEntry cashAndCashEquivalentsCalculation = createCalculationEntry(CASHANDCASHEQUIVALENTS_FIELD_NAME);

        if (reportingFormContainsEntries(report, cashAndCashEquivalentsCalculation)) {
            XbrlContext cashPrecedingContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(precedingPeriodEnd).build();
            XbrlValueEntry precedingValue = xbrlInstance.getValue(cashPrecedingContext, cashAndCashEquivalentsCalculation);

            XbrlContext cashReportingContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(reportingPeriodStart).build();
            XbrlValueEntry reportingValue = xbrlInstance.getValue(cashReportingContext, cashAndCashEquivalentsCalculation);

            if (reportingValue != null && precedingValue != null && !precedingValue.valueEquals(reportingValue)) {
                errors.add(new XbrlError.Builder().setCalculationEntryMetamodel(cashAndCashEquivalentsCalculation)
                        .setBaseValue(precedingValue.getValueAsBigDecimal())
                        .setCompareValue(reportingValue.getValueAsBigDecimal())
                        .setCode(RULE_R05)
                        .setArguments(null, formatDate(precedingPeriodEnd), xbrlInstance.getDisplayValue(precedingValue.getValueAsBigDecimal()),
                                formatDate(reportingPeriodStart), xbrlInstance.getDisplayValue(reportingValue.getValueAsBigDecimal()))
                        .build());
            }
        }

    }

    /**
     * R06 Cash for the end of period equals to Cash for start of period plus Change in Cash
     */
    private void calculateCashChange(XbrlInstance xbrlInstance, List<XbrlError> errors,
            List<XbrlContext> contexts, ReportingFormMetamodel report) {

        XbrlContext precedingContext = contexts.get(0);
        checkCashSum(precedingContext.getPeriodStartDate(), precedingContext.getPeriodEndDate(), xbrlInstance, report, errors);

        XbrlContext reportingContext = contexts.get(1);
        checkCashSum(reportingContext.getPeriodStartDate(), reportingContext.getPeriodEndDate(), xbrlInstance, report, errors);
    }

    private void checkCashSum(Date periodStart, Date periodEnd, XbrlInstance xbrlInstance,
            ReportingFormMetamodel report, List<XbrlError> errors) {
        CalculationEntry cashCalculation = createCalculationEntry(CASHANDCASHEQUIVALENTS_FIELD_NAME);
        CalculationEntry increaseDecreaseCalculation = createCalculationEntry(INCREASEDECREASEINCASHANDCASHEQUIVALENTS_FIELD_NAME);

        if (reportingFormContainsEntries(report, cashCalculation, increaseDecreaseCalculation)) {
            XbrlContext cashStartContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(periodStart).build();
            XbrlContext cashEndContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setInstant(periodEnd).build();
            XbrlContext increaseDecreaseContext = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA, XbrlBuilderImpl.IDENTIFIER).setDateRange(periodStart, periodEnd).build();

            XbrlValueEntry cashStartValue = xbrlInstance.getValue(cashStartContext, cashCalculation);
            XbrlValueEntry cashEndValue = xbrlInstance.getValue(cashEndContext, cashCalculation);
            XbrlValueEntry increaseDecreaseValue = xbrlInstance.getValue(increaseDecreaseContext, increaseDecreaseCalculation);

            if (cashEndValue != null && cashStartValue != null && increaseDecreaseValue != null) {
                BigDecimal cashStartIncreaseDecreaseSum = cashStartValue.getValueAsBigDecimal().add(increaseDecreaseValue.getValueAsBigDecimal());
                if (!cashEndValue.valueEquals(cashStartIncreaseDecreaseSum)) {
                    errors.add(new XbrlError.Builder()
                            .setBaseValue(cashEndValue.getValueAsBigDecimal())
                            .setCompareValue(cashStartIncreaseDecreaseSum)
                            .setCode(RULE_R06)
                            .setArguments(null, formatDate(periodEnd), xbrlInstance.getDisplayValue(cashEndValue.getValueAsBigDecimal()),
                                    formatDate(periodStart), increaseDecreaseContext.getPeriodLabel(), xbrlInstance.getDisplayValue(cashStartIncreaseDecreaseSum))
                            .build());
                }
            }
        }
    }

    /**
     * R07 Compliance of Profit and Comprehensive income reported in Income Statement, Cash Flows and Change in Equity reports
     */
    private void calculateProfitEquivalence(XbrlInstance xbrlInstance, ReportingFormMetamodel report, List<XbrlError> errors, String lang) {
        CalculationEntry profitLossEntry = createCalculationEntry(PROFITLOSS_FIELD_NAME);
        CalculationEntry otherComprehensiveIncomeEntry = createCalculationEntry(OTHERCOMPREHENSIVEINCOME_FIELD_NAME);
        CalculationEntry comprehensiveIncomeEntry = createCalculationEntry(COMPREHENSIVEINCOME_FIELD_NAME);

        for (DimensionEntry dimension : report.getDimension()) {
            List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
            for (DimensionEntry dimensionDomain : dimensionDomains) {
                for (XbrlContext context : xbrlInstance.getContexts().values()) {
                    if (context.getPeriodType() == PresentationEntry.PeriodType.DURATION && !context.hasExplicitMember()) {
                        calculateProfitEquivalenceEntry(profitLossEntry, context, xbrlInstance, dimensionDomain, errors, report, lang);
                        calculateProfitEquivalenceEntry(otherComprehensiveIncomeEntry, context, xbrlInstance, dimensionDomain, errors, report, lang);
                        calculateProfitEquivalenceEntry(comprehensiveIncomeEntry, context, xbrlInstance, dimensionDomain, errors, report, lang);
                    }
                }
            }
        }
    }

    private void calculateProfitEquivalenceEntry(CalculationEntry calculationEntry, XbrlContext context, XbrlInstance xbrlInstance, DimensionEntry dimensionDomain, List<XbrlError> errors, ReportingFormMetamodel report, String lang) {
        XbrlValueEntry entryMembersValue = xbrlInstance.getValue(context, calculationEntry, dimensionDomain);
        XbrlValueEntry entryValue = xbrlInstance.getValue(context, calculationEntry);
        if (entryValue != null && entryMembersValue != null && !entryMembersValue.valueEquals(entryValue)) {
            String label = report.getPresentation(calculationEntry).getPreferredLabel(lang);
            errors.add(new XbrlError.Builder()
                .setBaseValue(entryValue.getValueAsBigDecimal())
                .setCompareValue(entryMembersValue.getValueAsBigDecimal())
                .setCode(RULE_R07)
                .setArguments(null, context.getPeriodLabel(), label, xbrlInstance.getDisplayValue(entryValue.getValueAsBigDecimal()),
                    xbrlInstance.getDisplayValue(entryMembersValue.getValueAsBigDecimal()))
                .build());
        }
    }

    private CalculationEntry createCalculationEntry(String name) {
        return new CalculationEntry.Builder().setNamespace(IFRS_FULL_NAMESPACE).setNamespacePrefix(IFRS_FULL_NAMESPACE_PREFIX).setName(name).build();
    }

    private List<XbrlContext> filterPeriodContexts(XbrlInstance xbrlInstance) {
        List<XbrlContext> contexts = new ArrayList<XbrlContext>();
        for (XbrlContext context : xbrlInstance.getContexts().values()) {
            if (context.getPeriodType() == PresentationEntry.PeriodType.DURATION && !context.hasExplicitMember()) {
                contexts.add(context);
            }
        }
        if (contexts.size() != 2) {
            throw new RuntimeException("XbrlInstance should have at least two contexts");
        }
        Collections.sort(contexts, new Comparator<XbrlContext>() {
            @Override
            public int compare(XbrlContext context1, XbrlContext context2) {
                int startCompare = context1.getPeriodStartDate().compareTo(context2.getPeriodStartDate());
                return startCompare != 0 ? startCompare : context1.getPeriodEndDate().compareTo(context2.getPeriodEndDate());
            }
        });
        return contexts;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

}
