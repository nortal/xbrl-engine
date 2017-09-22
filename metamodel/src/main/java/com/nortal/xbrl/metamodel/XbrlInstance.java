package com.nortal.xbrl.metamodel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.LinkedEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;

public class XbrlInstance implements Serializable {

	private static final long serialVersionUID = 7065930282286933403L;
	private String schemaType;
	private String schemaHref;
	private XbrlValueEntry.Multiplier multiplier;

	private Map<String, XbrlContext> contexts;
	private Map<String, XbrlUnit> units;
	private Map<String, XbrlValueEntry> values;

	private transient ValueEntryDisplayValueMap valueEntryDisplayValues;

	public XbrlInstance() {
		contexts = new TreeMap<>();
		units = new TreeMap<>();
		values = new TreeMap<>();
	}

	public String getSchemaType() {
		return schemaType;
	}

	public void setSchemaType(String schemaType) {
		this.schemaType = schemaType;
	}

	public String getSchemaHref() {
		return schemaHref;
	}

	public void setSchemaHref(String schemaHref) {
		this.schemaHref = schemaHref;
	}

	public Map<String, XbrlContext> getContexts() {
		return contexts;
	}

	public void setContexts(Map<String, XbrlContext> contexts) {
		this.contexts = new TreeMap<String, XbrlContext>(contexts);
	}

	public void addContext(XbrlContext context) {
		this.contexts.put(context.getId(), context);
	}

	public XbrlContext getContext(String contextId) {
		return this.contexts.get(contextId);
	}

	public Map<String, XbrlUnit> getUnits() {
		return units;
	}

	public void setUnits(Map<String, XbrlUnit> units) {
		this.units = new TreeMap<String, XbrlUnit>(units);
	}

	public void addUnit(XbrlUnit unit) {
		this.units.put(unit.getId(), unit);
	}

	public XbrlUnit getUnit(String unitId) {
		return this.units.get(unitId);
	}

	public XbrlValueEntry.Multiplier getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(XbrlValueEntry.Multiplier multiplier) {
		this.multiplier = multiplier;
	}

	public Map<String, XbrlValueEntry> getValues() {
		return values;
	}

	public List<XbrlValueEntry> getValues(LinkedEntry entry) {
		List<XbrlValueEntry> values = new ArrayList<XbrlValueEntry>();

		for (XbrlValueEntry value : this.values.values()) {
			if (value.getNamespace().equals(entry.getNamespace()) && value.getName().equals(entry.getName())) {
				values.add(value);
			}
		}

		return values;
	}

	public void setValues(Map<String, XbrlValueEntry> values) {
		this.values = new TreeMap<>(values);
	}

	public void addValue(XbrlValueEntry value) {
		this.values.put(value.getId(), value);
	}

	public XbrlValueEntry getValue(String valueId) {
		return this.values.get(valueId);
	}

	public XbrlValueEntry getValue(XbrlContext context, LinkedEntry entry) {
		return getValue(context, entry, null);
	}

	public XbrlValueEntry getValue(XbrlContext context, LinkedEntry entry, DimensionEntry dimension) {
		XbrlContext.Builder contextBuilder = new XbrlContext.Builder();
		contextBuilder.merge(context);

		if (entry instanceof PresentationEntry) {
			contextBuilder.setDateRange((PresentationEntry) entry, context.getPeriodStartDate(),
					context.getPeriodEndDate());
		}

		if (dimension != null) {
			contextBuilder.setScenario(dimension.getAxis(), dimension);
		}

		return getValue(new XbrlValueEntry.Builder().setContextId(contextBuilder.buildId())
				.setNamespace(entry.getNamespace())
				.setNamespacePrefix(entry.getNamespacePrefix())
				.setName(entry.getName()).buildId());
	}

	public String getDisplayValue(BigDecimal bigDecimal) {
		NumberFormat df = new DecimalFormat();
		df.setGroupingUsed(false);
		return df.format(bigDecimal);
	}

	public String getDisplayValue(XbrlValueEntry valueEntry) {
		return getDisplayValue(valueEntry.getId());
	}

	public String getDisplayValue(String key) {
		Object displayValue = getDisplayValues().get(key);
		return displayValue == null ? null : displayValue.toString();
	}

	public String getDisplayValueForPdf(String key) {
		XbrlValueEntry valueEntry = getValues().get(key);

		if (valueEntry == null) {
			return null;
		}
		if (valueEntry.isMonetary() && !valueEntry.isEmpty()) {
			DecimalFormat df = new DecimalFormat();
			df.setGroupingUsed(true);
			df.setNegativePrefix("(");
			df.setNegativeSuffix(")");
			return df.format(valueEntry.getValueAsBigDecimal()
					.divide(BigDecimal.valueOf(getMultiplier().getValue()), BigDecimal.ROUND_HALF_UP));
		} else if (valueEntry.isPerShare() && !valueEntry.isEmpty()) {
			DecimalFormat df = new DecimalFormat("0.000");
			df.setGroupingUsed(true);
			df.setNegativePrefix("(");
			df.setNegativeSuffix(")");
			return df.format(valueEntry.getValueAsBigDecimal());
		}
		return valueEntry.getValue();
	}

	public Map<String, Object> getDisplayValues() {
		if (valueEntryDisplayValues == null) {
			valueEntryDisplayValues = new ValueEntryDisplayValueMap(this);
		}
		return valueEntryDisplayValues;
	}

	public void resetValueEntriesDirtyFlags() {
		for (XbrlValueEntry xbrlValueEntry : this.values.values()) {
			xbrlValueEntry.setIsDirty(false);
		}
	}

}
