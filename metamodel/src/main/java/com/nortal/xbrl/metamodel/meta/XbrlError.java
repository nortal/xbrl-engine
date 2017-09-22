package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;
import java.math.BigDecimal;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class XbrlError implements Serializable {
	private static final long serialVersionUID = 4202037169151042485L;

	private CalculationEntry calculationEntry;
	private XbrlValueEntry xbrlValueEntry;
	private XbrlContext context;

	private BigDecimal baseValue;
	private BigDecimal compareValue;

	private String code;
	private Object[] arguments;

	public CalculationEntry getCalculationEntryMetamodel() {
		return calculationEntry;
	}

	public XbrlValueEntry getValueEntryModel() {
		return xbrlValueEntry;
	}

	public XbrlContext getContext() {
		return context;
	}

	public BigDecimal getBaseValue() {
		return baseValue;
	}

	public BigDecimal getCompareValue() {
		return compareValue;
	}

	@Override
	public String toString() {
		return "XbrlError{" + "valueEntryModel=" + xbrlValueEntry + ", context=" + context + ", baseValue="
				+ baseValue + ", compareValue=" + compareValue + '}';
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof XbrlError)) {
			return false;
		}

		XbrlError other = (XbrlError) obj;
		EqualsBuilder equalsBuilder = new EqualsBuilder();
		equalsBuilder.append(code, other.code);
		equalsBuilder.append(arguments, other.arguments);

		return equalsBuilder.isEquals();
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(code);
		hashCodeBuilder.append(arguments);
		return hashCodeBuilder.toHashCode();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public void setArguments(Object[] arguments) {
		this.arguments = arguments;
	}

	public static class Builder {

		private CalculationEntry calculationEntry;
		private XbrlValueEntry xbrlValueEntry;
		private XbrlContext context;
		private BigDecimal baseValue;
		private BigDecimal compareValue;

		private String code;
		private Object[] arguments;

		public Builder setArguments(Object... arguments) {
			this.arguments = arguments;
			return this;
		}

		public Builder setCode(String code) {
			this.code = code;
			return this;
		}

		public Builder setCalculationEntryMetamodel(CalculationEntry calculationEntry) {
			this.calculationEntry = calculationEntry;
			return this;
		}

		public Builder setValueEntryModel(XbrlValueEntry xbrlValueEntry) {
			this.xbrlValueEntry = xbrlValueEntry;
			return this;
		}

		public Builder setContext(XbrlContext context) {
			this.context = context;
			return this;
		}

		public Builder setBaseValue(BigDecimal baseValue) {
			this.baseValue = baseValue;
			return this;
		}

		public Builder setCompareValue(BigDecimal compareValue) {
			this.compareValue = compareValue;
			return this;
		}

		public XbrlError build() {
			XbrlError error = new XbrlError();

			error.calculationEntry = calculationEntry;
			error.xbrlValueEntry = xbrlValueEntry;
			error.context = context;

			error.baseValue = baseValue;
			error.compareValue = compareValue;
			error.code = code;
			error.arguments = arguments;

			return error;
		}

	}

}
