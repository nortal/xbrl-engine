package com.nortal.xbrl;

import com.nortal.xbrl.model.ReportingFormType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.XbrlFieldValue;

public interface XbrlBuilder {

	enum PeriodRange {
		DURATION, DURATION_COMPARATIVE, INSTANT, INSTANT_COMPARATIVE
	}

	List<XbrlContext> getContexts(ReportingFormMetamodel formMetamodel, PeriodRange periodRange, Date periodStart,
                                  Date periodEnd, Date previousPeriodStart, Date previousPeriodEnd);

	List<XbrlContext> getFormContexts(PeriodRange periodRange, Date periodStart, Date periodEnd,
                                      Date previousPeriodStart, Date previousPeriodEnd);

	List<XbrlContext> getVisibleFormContexts(PeriodRange periodRange, Date periodStart, Date periodEnd,
                                             Date previousPeriodStart, Date previousPeriodEnd);

	PeriodRange getPeriodRange(ReportingFormType reportingFormType);

	Builder createBuilder();

	interface Builder {

		Builder setPeriodRanges(Map<String, PeriodRange> periodRanges);

		Builder setReportingForms(List<ReportingFormMetamodel> reportingForms);

		Builder setPeriodStart(Date periodStart);

		Builder setPeriodEnd(Date periodEnd);

		Builder setPreviousPeriodStart(Date previousPeriodStart);

		Builder setPreviousPeriodEnd(Date previousPeriodEnd);

		Builder setMultiplier(XbrlValueEntry.Multiplier multiplier);

		XbrlInstance build();

		XbrlInstance build(List<XbrlFieldValue> values);

		XbrlInstance populate(XbrlInstance instance, List<XbrlFieldValue> values);

		List<XbrlFieldValue> build(XbrlInstance xbrlInstance);

	}

}
