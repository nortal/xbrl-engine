package com.nortal.xbrl.impl;

import com.nortal.xbrl.XbrlBuilder;
import com.nortal.xbrl.model.ReportingFormType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.LabelEntry;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.metamodel.XbrlFieldValue;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;

public class XbrlBuilderImpl implements XbrlBuilder {

	public static final String SCHEMA = "Schema";
	public static final String IDENTIFIER = "identifier";

	// todo these values should not be hard coded
	private static final String SCHEMA_TYPE = "simple";
	private static final String SCHEMA_HREF = "schema.xsd";
	private static final List<Currency> AVAILABLE_CURRENCIES = Arrays.asList(Currency.getInstance("EUR"));

	protected List<XbrlUnit> createUnits(List<Currency> currencies) {
		if (currencies == null || currencies.isEmpty()) {
			throw new RuntimeException("Currency list can not be null or empty");
		}
		List<XbrlUnit> units = new ArrayList<XbrlUnit>();

		for (Currency currency : currencies) {
			units.add(new XbrlUnit.Builder().setMeasure(currency.getCurrencyCode()).build());
			units.add(new XbrlUnit.Builder().setUnitNumeratorMeasure(currency.getCurrencyCode())
					.setUnitDenominatorMeasure("shares").build());
		}

		return units;
	}

	@Override
	public List<XbrlContext> getContexts(ReportingFormMetamodel formMetamodel, PeriodRange periodRange,
			Date periodStart, Date periodEnd, Date previousPeriodStart, Date previousPeriodEnd) {
		List<XbrlContext> contexts = new ArrayList<XbrlContext>();
		List<XbrlContext> rootContexts = getFormContexts(periodRange, periodStart, periodEnd, previousPeriodStart,
				previousPeriodEnd);
		List<XbrlContext> flatContexts = getFlatContexts(formMetamodel, rootContexts);
		List<XbrlContext> dimensionsContexts = getDimensionContexts(formMetamodel, rootContexts);
		contexts.addAll(rootContexts);
		contexts.addAll(flatContexts);
		contexts.addAll(dimensionsContexts);
		return contexts;
	}

	@Override
	public List<XbrlContext> getFormContexts(PeriodRange periodRange, Date periodStart, Date periodEnd,
			Date previousPeriodStart, Date previousPeriodEnd) {
		List<XbrlContext> contexts = new ArrayList<XbrlContext>();
		switch (periodRange) {
		case DURATION:
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setDateRange(periodStart, periodEnd).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodStart).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodEnd).build());
			break;
		case DURATION_COMPARATIVE:
			// Case: Duration (might contain instant values as well) + Previous Duration
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setDateRange(previousPeriodStart,	previousPeriodEnd).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setDateRange(periodStart, periodEnd).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(previousPeriodStart).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(previousPeriodEnd).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodStart).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodEnd).build());
			break;
		case INSTANT:
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodEnd).build());
			break;
		case INSTANT_COMPARATIVE:
			// Case: Instant + Previous Instant
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(previousPeriodEnd).build());
			contexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setInstant(periodEnd).build());
			break;
		}
		return contexts;
	}

	public List<XbrlContext> getVisibleFormContexts(PeriodRange periodRange, Date periodStart, Date periodEnd, Date previousPeriodStart, Date previousPeriodEnd) {
		List<XbrlContext> contexts = getFormContexts(periodRange, periodStart, periodEnd, previousPeriodStart, previousPeriodEnd);
		List<XbrlContext> visibleContexts = new ArrayList<XbrlContext>();

		if(periodRange == PeriodRange.DURATION || periodRange == PeriodRange.DURATION_COMPARATIVE) {
			for(XbrlContext context : contexts) {
				if(context.getPeriodType() != PresentationEntry.PeriodType.INSTANT) {
					visibleContexts.add(context);
				}
			}
		} else {
			visibleContexts.addAll(contexts);
		}

		Collections.sort(visibleContexts, new Comparator<XbrlContext>() {
			@Override
			public int compare(XbrlContext o1, XbrlContext o2) {
				return o2.getPeriodEndDate().compareTo(o1.getPeriodEndDate());
			}
		});

		return visibleContexts;
	}

	@Override
	public PeriodRange getPeriodRange(ReportingFormType reportingFormType) {

		if (!reportingFormType.getStatementType().hasComparativePeriod()) {
			return XbrlBuilder.PeriodRange.DURATION;
		} else {
			if ("FP".equals(reportingFormType.getStatementType().getCode())) {
				return XbrlBuilder.PeriodRange.INSTANT_COMPARATIVE;
			} else {
				return XbrlBuilder.PeriodRange.DURATION_COMPARATIVE;
			}
		}
	}

	protected List<XbrlValueEntry> getFlatFormValues(ReportingFormMetamodel formMetamodel,
			List<XbrlContext> rootContexts, List<XbrlUnit> units) {
		// Create values for every presentation
		List<XbrlValueEntry> values = new ArrayList<XbrlValueEntry>();
		List<PresentationEntry> flatFormPresentation = formMetamodel.getFlatPresentation();
		for (PresentationEntry presentation : flatFormPresentation) {
			for (XbrlContext rootContext : rootContexts) {
				if (rootContext.hasExplicitMember() || presentation.isAbstract()) {
					continue;
				}

				if(presentation.getPeriod() != rootContext.getPeriodType()) {
					continue;
				}

				// Create context according to presentation
				XbrlContext.Builder contextBuilder = new XbrlContext.Builder().merge(rootContext).setDateRange(
						presentation, rootContext.getPeriodStartDate(), rootContext.getPeriodEndDate());

				XbrlValueEntry.Builder builder = new XbrlValueEntry.Builder()
						.setNamespace(presentation.getNamespace())
						.setNamespacePrefix(presentation.getNamespacePrefix())
						.setName(presentation.getName())
						.setContextId(contextBuilder.buildId());

				if (presentation.getType() == PresentationEntry.PresentationType.MONETARY) {
					builder.setUnitId(units.get(0).getId());
				} else if (presentation.getType() == PresentationEntry.PresentationType.PER_SHARE) {
					builder.setUnitId(units.get(1).getId());
				}

				values.add(builder.build());

			}
		}
		return values;
	}

	/**
	 * Create any additional context that might be needed for the flat form, ex: instance context for some fields
	 * 
	 * @param formMetamodel
	 * @param rootContexts
	 * @return
	 */
	protected List<XbrlContext> getFlatContexts(ReportingFormMetamodel formMetamodel, List<XbrlContext> rootContexts) {
		List<XbrlContext> flatContexts = new ArrayList<XbrlContext>();
		List<PresentationEntry> flatFormPresentation = formMetamodel.getFlatPresentation();

		for (PresentationEntry presentation : flatFormPresentation) {
			for (XbrlContext rootContext : rootContexts) {
				XbrlContext context = new XbrlContext.Builder().merge(rootContext)
						.setDateRange(presentation, rootContext.getPeriodStartDate(), rootContext.getPeriodEndDate())
						.build();

				if (!context.getId().equals(rootContext.getId())) {
					flatContexts.add(context);
				}
			}
		}

		return flatContexts;
	}

	protected List<XbrlContext> getDimensionContexts(ReportingFormMetamodel formMetamodel,
			List<XbrlContext> rootContexts) {
		List<XbrlContext> dimensionContexts = new ArrayList<XbrlContext>();

		for (DimensionEntry dimension : formMetamodel.getDimension()) {
			List<XbrlContext> axisContexts = getAxisContexts(dimension);

			if (axisContexts.isEmpty()) {
				continue;
			}

			// For each presentation that is a member of hypercube, iterate over axisContexts and create a new context
			// TODO Remove dublicated contexts
			List<PresentationEntry> lineItems = MetamodelUtil.getLineItems(formMetamodel
					.getPresentation(dimension));
			for (PresentationEntry presentation : lineItems) {
				for (XbrlContext axisContext : axisContexts) {
					for (XbrlContext rootContext : rootContexts) {
						XbrlContext.Builder contextBuilder = new XbrlContext.Builder();
						contextBuilder.merge(axisContext);
						contextBuilder.setDateRange(presentation, rootContext.getPeriodStartDate(),
								rootContext.getPeriodEndDate());
						dimensionContexts.add(contextBuilder.build());
					}
				}
			}
		}

		return dimensionContexts;
	}

	protected List<XbrlContext> getAxisContexts(DimensionEntry dimension) {
		List<XbrlContext> axisContexts = new ArrayList<XbrlContext>();

		for (DimensionEntry dimensionElement : dimension.getChildren()) {
			// Did we find the TABLE ?
			if (dimensionElement.getArcRole() != DimensionEntry.ArcRole.ALL) {
				continue;
			}

			for (DimensionEntry axis : dimensionElement.getChildren()) {
				List<DimensionEntry> axisDomainMembers = MetamodelUtil.getAxisDomainMembers(axis);

				for (DimensionEntry domainMember : axisDomainMembers) {
					axisContexts.add(new XbrlContext.Builder(SCHEMA, IDENTIFIER).setSegment(axis, domainMember)
							.build());
				}
			}
		}

		return axisContexts;
	}

	protected List<XbrlValueEntry> getDimensionValues(ReportingFormMetamodel formMetamodel, List<XbrlContext> dimensionContexts, List<XbrlUnit> units) {
		List<XbrlValueEntry> dimensionValues = new ArrayList<XbrlValueEntry>();
		for (DimensionEntry dimension : formMetamodel.getDimension()) {
			List<PresentationEntry> lineItems = MetamodelUtil.getLineItems(formMetamodel.getPresentation(dimension));
			for (PresentationEntry presentation : lineItems) {
				for (XbrlContext dimensionContext : dimensionContexts) {
					if ((presentation.getPeriod() == PresentationEntry.PeriodType.DURATION && dimensionContext.isDuration())
							|| (presentation.getPeriod() == PresentationEntry.PeriodType.INSTANT && dimensionContext.isInstant())) {
						
						if (presentation.getPeriod() == PresentationEntry.PeriodType.INSTANT && presentation.getPreferredLabelType() == LabelEntry.LabelType.PERIOD_START) {
							if (!contextMatchesPresentation(dimensionContexts, dimensionContext)) {
								continue;
							}
						}
						if (presentation.getType() == PresentationEntry.PresentationType.MONETARY) {
							dimensionValues.add(new XbrlValueEntry.Builder().setNamespace(presentation.getNamespace())
									.setNamespacePrefix(presentation.getNamespacePrefix()).setName(presentation.getName())
									.setContextId(dimensionContext.getId()).setUnit(units.get(0)).build());
						} else if (presentation.getType() == PresentationEntry.PresentationType.PER_SHARE) {
							dimensionValues.add(new XbrlValueEntry.Builder().setNamespace(presentation.getNamespace())
									.setNamespacePrefix(presentation.getNamespacePrefix()).setName(presentation.getName())
									.setContextId(dimensionContext.getId()).setUnit(units.get(1)).build());
						}
					}
				}
			}
		}
		return dimensionValues;
	}

	private boolean hasEqualPeriodContext(List<XbrlContext> durationContexts, XbrlContext context) {
		for (XbrlContext durationContext : durationContexts) {
			if (durationContext.getPeriodEndDate().equals(context.getPeriodEndDate())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean contextMatchesPresentation(List<XbrlContext> dimensionContexts, XbrlContext dimensionContext) {
		List<XbrlContext> durationContexts = new ArrayList<XbrlContext>();
		for (XbrlContext context : dimensionContexts) {
			if (durationContexts.size() == 2) {
				break;
			}
			if (context.getPeriodType() == PresentationEntry.PeriodType.DURATION) {
				if (hasEqualPeriodContext(durationContexts, context)) {
					continue;
				}
				durationContexts.add(context);
			}
		}

		for (XbrlContext durationContext : durationContexts) {
			if (durationContext.getPeriodStartDate().equals(dimensionContext.getPeriodEndDate())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public XbrlBuilder.Builder createBuilder() {
		return new Builder();
	}

	public class Builder implements XbrlBuilder.Builder {

		private Map<String, PeriodRange> periodRanges;
		private List<ReportingFormMetamodel> reportingForms;

		private Date periodStart;
		private Date periodEnd;

		private Date previousPeriodStart;
		private Date previousPeriodEnd;

		private XbrlValueEntry.Multiplier multiplier;

		@Override
		public Builder setPeriodRanges(Map<String, PeriodRange> periodRanges) {
			this.periodRanges = periodRanges;

			return this;
		}

		@Override
		public Builder setReportingForms(List<ReportingFormMetamodel> reportingForms) {
			this.reportingForms = reportingForms;

			return this;
		}

		@Override
		public Builder setPeriodStart(Date periodStart) {
			this.periodStart = periodStart;

			return this;
		}

		@Override
		public Builder setPeriodEnd(Date periodEnd) {
			this.periodEnd = periodEnd;

			return this;
		}

		@Override
		public Builder setPreviousPeriodStart(Date previousPeriodStart) {
			this.previousPeriodStart = previousPeriodStart;

			return this;
		}

		@Override
		public Builder setPreviousPeriodEnd(Date previousPeriodEnd) {
			this.previousPeriodEnd = previousPeriodEnd;

			return this;
		}

		@Override
		public XbrlBuilder.Builder setMultiplier(XbrlValueEntry.Multiplier multiplier) {
			this.multiplier = multiplier;

			return this;
		}

		@Override
		public XbrlInstance build() {
			// Check for required fields
			if (periodRanges == null || periodStart == null || periodEnd == null || previousPeriodStart == null
					|| previousPeriodEnd == null || multiplier == null) {
				throw new IllegalStateException("Some mandatory fields are missing.");
			}

			XbrlInstance instance = new XbrlInstance();
			instance.setSchemaHref(SCHEMA_HREF);
			instance.setSchemaType(SCHEMA_TYPE);
			instance.setMultiplier(multiplier);

			List<XbrlUnit> units = createUnits(AVAILABLE_CURRENCIES);
			for (XbrlUnit unit : units) {
				instance.addUnit(unit);
			}

			for (ReportingFormMetamodel form : reportingForms) {
				List<XbrlContext> rootContexts = getFormContexts(periodRanges.get(form.getCode()), periodStart,
						periodEnd, previousPeriodStart, previousPeriodEnd);

				List<XbrlValueEntry> values = getFlatFormValues(form, rootContexts, units);
				List<XbrlContext> contexts = getFlatContexts(form, rootContexts);

				// Create context and values for every root dimension
				List<XbrlContext> dimensionContexts = getDimensionContexts(form, rootContexts);
				List<XbrlValueEntry> dimensionValues = getDimensionValues(form, dimensionContexts, units);

				for (XbrlContext context : rootContexts) {
					instance.addContext(context);
				}
				for (XbrlContext context : contexts) {
					instance.addContext(context);
				}
				for (XbrlContext context : dimensionContexts) {
					instance.addContext(context);
				}

				for (XbrlValueEntry value : values) {
					instance.addValue(value);
				}

				for (XbrlValueEntry value : dimensionValues) {
					instance.addValue(value);
				}
			}

			return instance;
		}

		@Override
		public XbrlInstance build(List<XbrlFieldValue> values) {
			// Check for required fields
			if (reportingForms == null) {
				throw new IllegalStateException("Some mandatory fields are missing.");
			}

			XbrlInstance instance = new XbrlInstance();
			instance.setSchemaHref(SCHEMA_HREF);
			instance.setSchemaType(SCHEMA_TYPE);

			populate(instance, values);

			return instance;
		}

		@Override
		public XbrlInstance populate(XbrlInstance instance, List<XbrlFieldValue> values) {
			List<XbrlUnit> units = createUnits(AVAILABLE_CURRENCIES);
			for (XbrlUnit unit : units) {
				instance.addUnit(unit);
			}

			for (XbrlFieldValue fieldValue : values) {
				XbrlContext.Builder contextBuilder = new XbrlContext.Builder(XbrlBuilderImpl.SCHEMA,
						XbrlBuilderImpl.IDENTIFIER).setDateRange(fieldValue.getStartDate(), fieldValue.getEndDate());

				if (fieldValue.hasExplicitMembers()) {
					List<XbrlExplicitMember> xbrlExplicitMembers = fieldValue.getExplicitMembers();
					for (XbrlExplicitMember xbrlExplicitMember : xbrlExplicitMembers) {
						contextBuilder.setScenario(xbrlExplicitMember);
					}
				}

				XbrlContext xbrlContext = contextBuilder.build();

				XbrlValueEntry.Builder valueBuilder = new XbrlValueEntry.Builder().setContext(xbrlContext)
						.setNamespace(fieldValue.getValueNamespace())
						.setNamespacePrefix(fieldValue.getValueNamespacePrefix()).setName(fieldValue.getValueName())
						.setValue(fieldValue.getValue());

				if (fieldValue.getValueType() == XbrlFieldValue.ValueType.MONETARY) {
					valueBuilder.setUnit(units.get(0));
				} else if (fieldValue.getValueType() == XbrlFieldValue.ValueType.PERSHARE) {
					valueBuilder.setUnit(units.get(1));
				}

				instance.addContext(xbrlContext);
				instance.addValue(valueBuilder.build());
			}

			return instance;
		}

		@Override
		public List<XbrlFieldValue> build(XbrlInstance xbrlInstance) {
			List<XbrlFieldValue> fieldValues = new ArrayList<XbrlFieldValue>();

			for (XbrlValueEntry xbrlValueEntry : xbrlInstance.getValues().values()) {
				if (xbrlValueEntry.isDirty()) {
					XbrlContext xbrlContext = xbrlInstance.getContext(xbrlValueEntry.getContextId());
					String fullName = xbrlValueEntry.getId();
					fullName = fullName.substring(fullName.lastIndexOf(XbrlMetamodel.PARTS_DELIMITER,
							fullName.indexOf(XbrlMetamodel.PREFIX_DELIMITER)) + 1);

					fieldValues.add(new XbrlFieldValue.Builder()
							.setStartDate(xbrlContext.getPeriodStartDate())
							.setEndDate(xbrlContext.getPeriodEndDate())
							.setName(fullName)
							.setValueType(getValueType(xbrlValueEntry))
							.setValue(getValue(xbrlValueEntry)).build());
				}
			}

			return fieldValues;
		}

		private XbrlFieldValue.ValueType getValueType(XbrlValueEntry xbrlValueEntry) {
			if (xbrlValueEntry.isMonetary()) {
				return XbrlFieldValue.ValueType.MONETARY;
			} else if (xbrlValueEntry.isPerShare()) {
				return XbrlFieldValue.ValueType.PERSHARE;
			} else {
				return XbrlFieldValue.ValueType.TEXT;
			}
		}

		private String getValue(XbrlValueEntry xbrlValueEntry) {
			if (xbrlValueEntry.isMonetary()) {
				return getMonetaryValue(xbrlValueEntry);
			} else if (xbrlValueEntry.isPerShare()) {
				return getPerShareValue(xbrlValueEntry);
			} else {
				return xbrlValueEntry.getValue();
			}
		}

		private String getMonetaryValue(XbrlValueEntry value) {
			return value.isEmpty() ? null : value.getValueAsBigDecimal().toString();
		}

		private String getPerShareValue(XbrlValueEntry value) {
			return value.isEmpty() ? null : value.getValueAsBigDecimal().toString();
		}

	}

}
