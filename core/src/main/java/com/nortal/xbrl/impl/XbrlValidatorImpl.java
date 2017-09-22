package com.nortal.xbrl.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.nortal.xbrl.XbrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.CalculationEntry;
import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;

public class XbrlValidatorImpl implements XbrlValidator {

	private Logger LOG = LoggerFactory.getLogger(XbrlValidatorImpl.class);

	@Override
	public List<XbrlError> validate(ReportingFormMetamodel report, XbrlInstance instance, List<XbrlContext> contexts, String lang) {
		LOG.debug("Validating instance {} against report {}", instance, report);
		List<XbrlError> errors = new ArrayList<XbrlError>();
		calculateFlatForm(report, instance, errors, contexts, lang);
		calculateHypercubeForm(report, instance, errors, lang);
		return errors;
	}

	protected void calculateFlatForm(ReportingFormMetamodel reportingForm, XbrlInstance instance, List<XbrlError> errors, List<XbrlContext> formContexts, String lang) {
		for (CalculationEntry calculationEntry : reportingForm.getCalculation()) {
			LOG.debug("Calculating values for {}", calculationEntry);
			List<XbrlValueEntry> values = instance.getValues(calculationEntry);
			for (XbrlValueEntry value : values) {
				XbrlContext valueContext = instance.getContext(value.getContextId());

				// Check if this is context for hypercube
				if (valueContext.hasExplicitMember()) {
					continue;
				}

				try {
					calculateCalculationEntry(valueContext, instance, calculationEntry, reportingForm, errors, lang);
				}
				catch (XbrlEngineException e) {
					LOG.error("Validation: provided value is incorrect", e);
				}
			}
		}

		if (reportingForm.getCalculation().isEmpty()){
			if (!formContainsFlatFormData(instance, formContexts, reportingForm)) {
				String reportCode = reportingForm.getCode();
				String reportName = reportingForm.getName().get(lang);
				errors.add(new XbrlError.Builder()
						.setCode("xbrl.validate.error.template.emptyform")
						.setArguments(reportCode, reportName)
						.build());
			}
		}
	}

	protected BigDecimal calculateCalculationEntry(XbrlContext context, XbrlInstance instance,
			CalculationEntry calculationEntry, ReportingFormMetamodel reportingForm, List<XbrlError> errors, String lang) {

		XbrlValueEntry xbrlValueEntry = instance.getValue(context, calculationEntry);
		if (xbrlValueEntry == null) {
			LOG.error("Could not find calculation entry data {} in instance metamodel", calculationEntry);
			throw new XbrlEngineException("Could not find value for field.");
		}
		String reportName = reportingForm.getName().get(lang);
		String reportCode = reportingForm.getCode();
		if (xbrlValueEntry.getValue() != null) {
			if (xbrlValueEntry.getValue().contains(".")) {
				String label = createLabel(xbrlValueEntry, lang, reportingForm, context);
				errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.format")
						.setArguments(reportCode, reportName, label).build());
				throw new XbrlEngineException("Illegal argument");
			}
		}
		BigDecimal value;
		try {
			value = xbrlValueEntry.getValueAsBigDecimal();
		}
		catch (NumberFormatException e) {
			String label = createLabel(xbrlValueEntry, lang, reportingForm, context);
			errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.format")
					.setArguments(reportCode, reportName, label).build());
			throw new XbrlEngineException(e);
		}
		if (calculationEntry.getWeight() != null) {
			value = value.multiply(calculationEntry.getWeight());
		}
		if (calculationEntry.getChildren().isEmpty()) {
			LOG.debug("No children, value {}", value);
			return value;
		}
		if (xbrlValueEntry.isEmpty()) {
			String label = createLabel(xbrlValueEntry, lang, reportingForm, context);
			errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.novalue")
					.setArguments(reportCode, reportName, label).build());
		}
		BigDecimal sum = BigDecimal.ZERO;
		for (CalculationEntry childCalculationEntry : calculationEntry.getChildren()) {
			sum = sum.add(calculateCalculationEntry(context, instance, childCalculationEntry, reportingForm, errors, lang));
		}
		LOG.debug("Children sum {}", sum);
		if (value.compareTo(sum) != 0) {
			LOG.debug("Children sum does not match parent value {} != {}", sum, value);
			String label = createLabel(xbrlValueEntry, lang, reportingForm, context);
			errors.add(new XbrlError.Builder()
					.setCalculationEntryMetamodel(calculationEntry)
					.setValueEntryModel(xbrlValueEntry)
					.setContext(context)
					.setBaseValue(value)
					.setCompareValue(sum)
					.setCode("xbrl.validate.error.template")
					.setArguments(reportCode, reportName, instance.getDisplayValue(value), label,
							instance.getDisplayValue(sum)).build());
		}
		return value;
	}

	protected void calculateHypercubeForm(ReportingFormMetamodel reportingForm, XbrlInstance instance,
			List<XbrlError> errors, String lang) {
		// Dimension domains validation
		for (DimensionEntry dimension : reportingForm.getDimension()) {
			List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
			List<DimensionEntry> lineItems = MetamodelUtil.getLineItems(dimension);

			for (DimensionEntry item : lineItems) {
				LOG.debug("Validating dimension domains for {}", item);

				// Check if this line item is in calculation
				CalculationEntry calculationEntry = reportingForm.getCalculation(item);

				if (calculationEntry != null && !calculationEntry.getChildren().isEmpty()) {
					continue;
				}

				List<XbrlValueEntry> values = instance.getValues(item);

				// For every dimension domain find corresponding value and calculate children
				for (DimensionEntry dimensionDomain : dimensionDomains) {
					for (XbrlValueEntry valueEntry : values) {
						XbrlContext xbrlContext = instance.getContext(valueEntry.getContextId());

						if (!xbrlContext.hasExplicitMember(dimensionDomain.getAxis().getName(),
								dimensionDomain.getName())) {
							continue;
						}

						calculateDimensionDomain(instance, xbrlContext, dimensionDomain, values, errors, reportingForm, lang);
						crossForm(instance, xbrlContext, dimensionDomain, values, errors, reportingForm, lang);
					}
				}
			}
		}

		// Calculation entry validation
		for (CalculationEntry calculationEntry : reportingForm.getCalculation()) {
			// Is this calculation entry part of the hypercube
			if (reportingForm.getDimension(calculationEntry) == null) {
				continue;
			}

			LOG.debug("Validating values for {}", calculationEntry);

			List<XbrlValueEntry> values = instance.getValues(calculationEntry);

			for (XbrlValueEntry value : values) {
				XbrlContext valueContext = instance.getContext(value.getContextId());

				// Check if this is context for hypercube
				if (!valueContext.hasExplicitMember()) {
					continue;
				}

				try {
					calculateCalculationEntry(valueContext, instance, calculationEntry, reportingForm, errors, lang);
				}
				catch (XbrlEngineException e) {
					LOG.error("Validation: provided value is incorrect", e);
				}
			}
		}
	}

	private BigDecimal calculateDimensionDomain(XbrlInstance instance, XbrlContext xbrlContext,
			DimensionEntry dimensionEntry, List<XbrlValueEntry> values, List<XbrlError> errors,
			ReportingFormMetamodel report, String lang) {
		String name = report.getName().get(lang);
		String code = report.getCode();

		// Find dimension domain value
		XbrlValueEntry xbrlValueEntry = null;
		for (XbrlValueEntry valueEntry : values) {
			XbrlContext valueContext = instance.getContext(valueEntry.getContextId());

			// Check if the explicit member match
			if (!valueContext.hasExplicitMember(dimensionEntry.getAxis().getName(), dimensionEntry.getName())) {
				continue;
			}

			// Check if the period`s match
			if (!xbrlContext.hasEqualPeriod(valueContext)) {
				continue;
			}

			xbrlValueEntry = valueEntry;
		}

		if (xbrlValueEntry == null) {
			LOG.error("Could not find calculation entry data {} in instance metamodel", dimensionEntry);
			throw new XbrlEngineException("Could not find value for field.");
		}
		if (xbrlValueEntry.getValue() != null) {
			if (xbrlValueEntry.getValue().contains(".")) {
				String label = createLabel(xbrlValueEntry, lang, report, xbrlContext);
				errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.format")
						.setArguments(code, name, label).build());
				throw new XbrlEngineException("Illegal argument");
			}
		}
		BigDecimal value;
		try {
			value = xbrlValueEntry.getValueAsBigDecimal();
		}
		catch (NumberFormatException e) {
			String label = createLabel(xbrlValueEntry, lang, report, xbrlContext);
			errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.format")
					.setArguments(code, name, label).build());
			throw new XbrlEngineException(e);
		}
		if (dimensionEntry.getChildren().isEmpty()) {
			LOG.debug("No children, value {}", value);
			return value;
		}
		if (xbrlValueEntry.isEmpty()) {
			String label = createLabel(xbrlValueEntry, lang, report, xbrlContext);
			errors.add(new XbrlError.Builder().setCode("xbrl.validate.error.template.novalue")
					.setArguments(code, name, label).build());
		}
		BigDecimal sum = BigDecimal.ZERO;
		for (DimensionEntry childDimensionEntry : dimensionEntry.getChildren()) {
			if (!childDimensionEntry.getChildren().isEmpty()) {
				calculateDimensionDomain(instance, xbrlContext, childDimensionEntry, values, errors, report, lang);
			}
			sum = sum
					.add(calculateDimensionDomain(instance, xbrlContext, childDimensionEntry, values, errors, report, lang));
		}
		LOG.debug("Children sum {}", sum);
		if (value.compareTo(sum) != 0) {
			LOG.debug("Children sum does not match parent value {} != {}", sum, value);
			String label = createLabel(xbrlValueEntry, lang, report, xbrlContext);
			errors.add(new XbrlError.Builder()
					.setCalculationEntryMetamodel(
							new CalculationEntry.Builder().addChild(
									new CalculationEntry.Builder().setNamespace(dimensionEntry.getNamespace())
											.setName(dimensionEntry.getName()).build()).build())
					.setValueEntryModel(xbrlValueEntry).setContext(xbrlContext).setBaseValue(value)
					.setCompareValue(sum).setCode("xbrl.validate.error.template")
					.setArguments(code, name, instance.getDisplayValue(value), label, instance.getDisplayValue(sum))
					.build());
		}
		return value;
	}

	protected void crossForm(XbrlInstance instance, XbrlContext xbrlContext, DimensionEntry dimensionEntry,
			List<XbrlValueEntry> values, List<XbrlError> errors, ReportingFormMetamodel report, String lang) {
		String name = report.getName().get(lang);
		String code = report.getCode();

		// Find dimension domain value
		XbrlValueEntry dimensionDomainValueEntry = null;
		for (XbrlValueEntry valueEntry : values) {
			XbrlContext valueContext = instance.getContext(valueEntry.getContextId());

			// Check if the explicit member match
			if (!valueContext.hasExplicitMember(dimensionEntry.getAxis().getName(), dimensionEntry.getName())) {
				continue;
			}

			// Check if the period`s match
			if (!xbrlContext.hasEqualPeriod(valueContext)) {
				continue;
			}

			dimensionDomainValueEntry = valueEntry;
		}

		if (dimensionDomainValueEntry == null) {
			LOG.error("Could not find calculation entry data {} in instance metamodel", dimensionEntry);
			return;
		}

		if (dimensionDomainValueEntry.getValue() != null && dimensionDomainValueEntry.getValue().contains(".")) {
			return;
		}

		BigDecimal dimensionDomainValue;
		try {
			dimensionDomainValue = dimensionDomainValueEntry.getValueAsBigDecimal();
		}
		catch (NumberFormatException e) {
			return;
		}

		if (dimensionDomainValueEntry.isEmpty()) {
			return;
		}

		// Validate against all values that don`t have explicit member
		for (XbrlValueEntry valueEntry : values) {
			XbrlContext valueContext = instance.getContext(valueEntry.getContextId());

			// If has explicit member then this is a value from hypercube, ignore it
			if (valueContext.hasExplicitMember() || !valueContext.hasEqualPeriod(xbrlContext)) {
				continue;
			}

			BigDecimal flatValue;
			try {
				flatValue = valueEntry.getValueAsBigDecimal();
			}
			catch (NumberFormatException e) {
				return;
			}

			if (dimensionDomainValue.compareTo(flatValue) != 0) {
				LOG.debug("Flat form value does not match hypercube value {} != {}", flatValue, dimensionDomainValue);
				String label = createLabel(dimensionDomainValueEntry, lang, report, xbrlContext);
				errors.add(new XbrlError.Builder()
						.setCalculationEntryMetamodel(
								new CalculationEntry.Builder().addChild(
										new CalculationEntry.Builder()
												.setNamespace(dimensionEntry.getNamespace())
												.setName(dimensionEntry.getName()).build()).build())
						.setValueEntryModel(dimensionDomainValueEntry)
						.setContext(xbrlContext)
						.setBaseValue(dimensionDomainValue)
						.setCompareValue(flatValue)
						.setCode("xbrl.validate.error.template.crossvalue")
						.setArguments(code, name, instance.getDisplayValue(dimensionDomainValue), label,
								instance.getDisplayValue(flatValue)).build());
			}
		}
	}

	private String createLabel(XbrlValueEntry value, String lang, ReportingFormMetamodel report, XbrlContext context) {
		String label = findLabelFromReport(report.getPresentation(), value.getName(), value.getNamespace(), lang, context);

		if (!context.getSegments().isEmpty()) {
			XbrlExplicitMember explicitMember = context.getSegments().get(0);
			label += " for " + findLabelFromReport(report.getPresentation(), explicitMember.getValueName(), explicitMember.getValueNamespace(), lang, context);
			if (context.isDuration()) {
				label += " (" + XbrlMetamodel.DATE_FORMAT.get().format(context.getPeriodStartDate()) + " - "
						+ XbrlMetamodel.DATE_FORMAT.get().format(context.getPeriodEndDate()) + ")";
			}
		}

		return label;
	}

	private String findLabelFromReport(List<PresentationEntry> presentationEntries, String name, String namespace, String lang, XbrlContext context) {
		String label = null;

		for (PresentationEntry presentation : presentationEntries) {

			if (presentation.getName().equals(name) && presentation.getNamespace().equals(namespace)) {
				return getLabel(lang, context, presentation);
			}

			if (!presentation.getChildren().isEmpty()) {
				label = findLabelFromReport(presentation.getChildren(), name, namespace, lang, context);
			}

			if (label != null) {
				return label;
			}
		}

		return label;
	}

	private String getLabel(String lang, XbrlContext context, PresentationEntry presentation) {
		String res = presentation.getPreferredLabel(lang);

		if (presentation.getPeriod() == PresentationEntry.PeriodType.INSTANT) {
            res += " (" + XbrlMetamodel.DATE_FORMAT.get().format(context.getPeriodEndDate()) + ")";
        }
        else if (presentation.getPeriod() == PresentationEntry.PeriodType.DURATION  && context.getSegments().isEmpty()){
            res += " (" + XbrlMetamodel.DATE_FORMAT.get().format(context.getPeriodStartDate()) + " - "
                    + XbrlMetamodel.DATE_FORMAT.get().format(context.getPeriodEndDate()) + ")";
        }

		return res;
	}

	@Override
	public boolean formContainsData(XbrlInstance xbrlInstance, List<XbrlContext> xbrlContexts, ReportingFormMetamodel reportingFormMetamodel) {

		return formContainsFlatFormData(xbrlInstance, xbrlContexts, reportingFormMetamodel)
				|| formContainsHypercubeData(xbrlInstance, xbrlContexts, reportingFormMetamodel);
	}

	private boolean formContainsFlatFormData(XbrlInstance xbrlInstance, List<XbrlContext> formContexts, ReportingFormMetamodel metamodel) {
		List<XbrlValueEntry> valueEntries = new ArrayList<XbrlValueEntry>();

		for (PresentationEntry presentationEntry: metamodel.getFlatPresentation()) {
			valueEntries.addAll(xbrlInstance.getValues(presentationEntry));
		}

		for(XbrlContext formContextModel : formContexts) {
			for(XbrlValueEntry value : valueEntries) {
				if (value.getContextId().equals(formContextModel.getId()) && !value.isEmpty()) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean formContainsHypercubeData(XbrlInstance xbrlInstance, List<XbrlContext> contexts, ReportingFormMetamodel metamodel) {
		for (PresentationEntry presentationEntry : metamodel.getHypercubePresentation()) {
			for (XbrlContext context : contexts) {
				PresentationEntry table = presentationEntry.getChildren().get(0);
				PresentationEntry lineItems = presentationEntry.getChildren().get(1);
				PresentationEntry axis = table.getChildren().get(0);

				if (hypercubeAxisContainsData(lineItems, axis, metamodel, xbrlInstance, context)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean hypercubeAxisContainsData(PresentationEntry lineItems, PresentationEntry axis,
											  ReportingFormMetamodel metamodel, XbrlInstance instance, XbrlContext context) {

		if (!lineItems.isAbstract()) {
			if (axis.getType() != PresentationEntry.PresentationType.AXIS ) {
				if (instance.getValue(context, lineItems, metamodel.getDimension(axis)) != null)
					return instance.getValue(context, lineItems, metamodel.getDimension(axis)).getValue() != null;
			}
		}

		for (PresentationEntry axisChild : axis.getChildren()) {
			if (hypercubeAxisContainsData(lineItems, axisChild, metamodel, instance, context)) {
				return true;
			}
		}

		if (!lineItems.getChildren().isEmpty()) {
			for (PresentationEntry lineItem : lineItems.getChildren()) {
				if (hypercubeAxisContainsData(lineItem, axis, metamodel, instance, context)) {
					return true;
				}
			}
		}

		return false;
	}
}
