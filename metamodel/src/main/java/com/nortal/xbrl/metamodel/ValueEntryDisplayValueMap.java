package com.nortal.xbrl.metamodel;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ValueEntryDisplayValueMap extends AbstractMap<String, Object> {

	private final XbrlInstance xbrlInstance;

	public ValueEntryDisplayValueMap(XbrlInstance xbrlInstance) {
		this.xbrlInstance = xbrlInstance;
	}

	@Override
	public String get(Object key) {
		XbrlValueEntry valueEntry = getValues().get(key);
		return getDisplayValue(valueEntry);
	}

	@Override
	public String put(String key, Object value) {
		String stringValue;

		if (value.getClass().isArray()){
			Object[] objects = (Object[]) value;
			stringValue = (String) objects[0];
		} else {
			stringValue = (String) value;
		}

		XbrlValueEntry valueEntry = getValues().get(key);
		String oldValue = getDisplayValue(valueEntry);

		// TODO is null allowed? create a new value entry if null?
		if (valueEntry == null) {
			return oldValue;
		}

		if (stringValue != null && !stringValue.isEmpty()) {
			if (valueEntry.isMonetary()) {
				try {
					BigDecimal decimalValue = new BigDecimal(stringValue).multiply(getMultiplier());
					validateValueLength(decimalValue.toBigInteger());
					valueEntry.setValue(decimalValue);
				}
				catch (Exception ignored) {
				}
			} else if (valueEntry.isPerShare()) {
				try {
					BigDecimal decimalValue = new BigDecimal(stringValue).setScale(3, BigDecimal.ROUND_HALF_UP);
					validateValueLength(decimalValue.toBigInteger());
					valueEntry.setValue(decimalValue);
				}
				catch (Exception ignored) {
				}
			} else {
				valueEntry.setValue(stringValue.substring(0, Math.min(stringValue.length(), 2000)));
			}
		} else {
			valueEntry.setValue(stringValue);
		}

		return oldValue;
	}

	private void validateValueLength(BigInteger number) throws Exception {
		if (number.compareTo(BigInteger.ZERO) < 0 && number.toString().length() > 21) {
			throw new IllegalArgumentException();
		} else if (number.compareTo(BigInteger.ZERO) >= 0 && number.toString().length() > 20) {
			throw new IllegalArgumentException();
		}
	}

	private String getDisplayValue(XbrlValueEntry valueEntry) {
		if (valueEntry == null) {
			return null;
		}
		if (valueEntry.isMonetary() && !valueEntry.isEmpty()) {
			NumberFormat df = new DecimalFormat();
			df.setGroupingUsed(false);
			return df.format(valueEntry.getValueAsBigDecimal().divide(getMultiplier(), BigDecimal.ROUND_HALF_UP));
		} else if (valueEntry.isPerShare() && !valueEntry.isEmpty()) {
			NumberFormat df = new DecimalFormat("0.000");
			df.setGroupingUsed(false);
			return df.format(valueEntry.getValueAsBigDecimal());
		}
		return valueEntry.getValue();
	}

	private BigDecimal getMultiplier() {
		return BigDecimal.valueOf(xbrlInstance.getMultiplier().getValue());
	}

	private Map<String, XbrlValueEntry> getValues() {
		return xbrlInstance.getValues();
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public Set<Entry<String, Object>> entrySet() {
		Set<Entry<String, Object>> entrySet = new HashSet<Entry<String, Object>>();
		for (Entry<String, XbrlValueEntry> valueEntry : getValues().entrySet()) {
			entrySet.add(new SimpleEntry<String, Object>(valueEntry.getKey(), getDisplayValue(valueEntry.getValue())));
		}
		return entrySet;
	}

}
