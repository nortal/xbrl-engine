package com.nortal.xbrl.impl;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.CalculationEntry;
import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.XbrlCalculator;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;

public class XbrlCalculatorImpl implements XbrlCalculator {

	private Logger LOG = LoggerFactory.getLogger(XbrlCalculatorImpl.class);

	@Override
	public XbrlInstance calculate(ReportingFormMetamodel report, XbrlInstance instance) {
		LOG.debug("Calculating instance {} against report {}", instance, report);
		calculateFlatForm(report, instance);
		calculateHypercubeForm(report, instance);
		return instance;
	}

	protected void calculateFlatForm(ReportingFormMetamodel report, XbrlInstance instance) {
		for (CalculationEntry calculationEntry : report.getCalculation()) {
			LOG.debug("Calculating values for {}", calculationEntry);

			// Get all values that match the calculation entry
			List<XbrlValueEntry> values = instance.getValues(calculationEntry);

			// Every value has a different context, so use this information to do calculation
			for (XbrlValueEntry value : values) {
				XbrlContext valueContext = instance.getContext(value.getContextId());

				// Check if this is context for hypercube
				if (valueContext.hasExplicitMember()) {
					continue;
				}

				calculateCalculationEntry(valueContext, instance, calculationEntry);
			}
		}
	}

	protected BigDecimal calculateCalculationEntry(XbrlContext context, XbrlInstance instance,
			CalculationEntry calculationEntry) {
		XbrlValueEntry xbrlValueEntry = instance.getValue(context, calculationEntry);
		if (xbrlValueEntry == null) {
			LOG.error("Could not find calculation entry data {} in instance metamodel", calculationEntry);
			throw new XbrlEngineException("Could not find value for field.");
		}
		BigDecimal value = xbrlValueEntry.getValueAsBigDecimal();
		if (calculationEntry.getWeight() != null) {
			value = value.multiply(calculationEntry.getWeight());
		}
		if (calculationEntry.getChildren().isEmpty()) {
			LOG.debug("No children, value {}", value);
			return value;
		}
		BigDecimal sum = BigDecimal.ZERO;
		for (CalculationEntry childCalculationEntry : calculationEntry.getChildren()) {
			if (!childCalculationEntry.getChildren().isEmpty()) {
				calculateCalculationEntry(context, instance, childCalculationEntry);
			}
			sum = sum.add(calculateCalculationEntry(context, instance, childCalculationEntry));
		}
		LOG.debug("Children sum {}", sum);
		xbrlValueEntry.setValue(sum);
		return value;
	}

	protected void calculateHypercubeForm(ReportingFormMetamodel report, XbrlInstance instance) {
		// Dimension domains
		for (DimensionEntry dimension : report.getDimension()) {
			List<DimensionEntry> dimensionDomains = MetamodelUtil.getDimensionDomains(dimension);
			List<DimensionEntry> lineItems = MetamodelUtil.getLineItems(dimension);

			for (DimensionEntry item : lineItems) {
				LOG.debug("Calculating dimension domains for {}", item);

				// Check if this line item is in calculation
				CalculationEntry calculationEntry = report.getCalculation(item);

				if (calculationEntry != null && !calculationEntry.getChildren().isEmpty()) {
					continue;
				}

				List<XbrlValueEntry> values = instance.getValues(item);

				// For every dimension domain find corresponding value and calculate children
				for (DimensionEntry dimensionDomain : dimensionDomains) {
					for (XbrlValueEntry valueEntry : values) {
						XbrlContext valueContext = instance.getContext(valueEntry.getContextId());

						if (!valueContext.hasExplicitMember(dimensionDomain.getAxis().getName(),
								dimensionDomain.getName())) {
							continue;
						}

						calculateDimensionDomainChildren(valueContext, instance, dimensionDomain, values);
					}
				}
			}
		}

		// Calculation entry
		for (CalculationEntry calculationEntry : report.getCalculation()) {
			// Is this calculation entry part of the hypercube
			if (report.getDimension(calculationEntry) == null) {
				continue;
			}

			LOG.debug("Calculating values for {}", calculationEntry);

			// Get all values that match the calculation entry
			List<XbrlValueEntry> values = instance.getValues(calculationEntry);

			// Every value has a different context, so use this information to do calculation
			for (XbrlValueEntry value : values) {
				XbrlContext valueContext = instance.getContext(value.getContextId());

				// Check if this is context for hypercube
				if (!valueContext.hasExplicitMember()) {
					continue;
				}

				calculateCalculationEntry(valueContext, instance, calculationEntry);
			}
		}
	}

	protected BigDecimal calculateDimensionDomainChildren(XbrlContext xbrlContext, XbrlInstance instance,
			DimensionEntry dimensionEntry, List<XbrlValueEntry> values) {
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

		BigDecimal value = xbrlValueEntry.getValueAsBigDecimal();
		if (dimensionEntry.getChildren().isEmpty()) {
			LOG.debug("No children, value {}", value);
			return value;
		}

		BigDecimal sum = BigDecimal.ZERO;
		for (DimensionEntry childDimensionEntry : dimensionEntry.getChildren()) {
			if (!childDimensionEntry.getChildren().isEmpty()) {
				calculateDimensionDomainChildren(xbrlContext, instance, childDimensionEntry, values);
			}
			sum = sum.add(calculateDimensionDomainChildren(xbrlContext, instance, childDimensionEntry, values));
		}

		LOG.debug("Children sum {}", sum);
		xbrlValueEntry.setValue(sum);
		return value;
	}

}
