package com.nortal.xbrl.metamodel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import org.apache.commons.lang3.StringUtils;

public class XbrlValueEntry implements Serializable {

	private static final long serialVersionUID = -1722565230682301973L;

	public enum Multiplier {
		ONE(1, 0), THOUSAND(1000, -3);

		private int value;
		private int decimals;

		Multiplier(int value, int decimals) {
			this.value = value;
			this.decimals = decimals;
		}

		public int getValue() {
			return value;
		}

		public int getDecimals() {
			return decimals;
		}
	}

	protected String id;

	protected String namespace;
	protected String namespacePrefix;
	protected String name;

	protected String value;
	protected Integer decimals;
	protected String precision;

	protected String contextId;
	protected String unitId;

	protected boolean dirty;

	public XbrlValueEntry() {
		super();
	}

	public String getId() {
		return id;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getNamespacePrefix() {
		return namespacePrefix;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public BigDecimal getValueAsBigDecimal() {
		return getValue() == null ? BigDecimal.ZERO : new BigDecimal(getValue());
	}

	public void setValue(BigDecimal value) {
		NumberFormat df;

		df = new DecimalFormat();
		df.setGroupingUsed(false);

		setValue(df.format(value));
	}

	public void setValue(String value) {
		value = (value == null || value.isEmpty()) ? null : value;

		if (!StringUtils.equals(value, this.value)) {
			dirty = true;
		}

		this.value = value;
	}

	public Integer getDecimals() {
		return decimals;
	}

	public String getPrecision() {
		return precision;
	}

	public boolean isMonetary() {
		return unitId != null && !isPerShare();
	}

	public boolean isPerShare() {
		return unitId != null && unitId.endsWith("perShare");
	}

	public String getContextId() {
		return contextId;
	}

	public String getUnitId() {
		return unitId;
	}

	public boolean isDirty() {
		return this.dirty;
	}

	public void setIsDirty(boolean isDirty) {
		this.dirty = isDirty;
	}

	public boolean valueEquals(XbrlValueEntry xbrlValueEntry) {
		return valueEquals(xbrlValueEntry.getValueAsBigDecimal());
	}

	public boolean valueEquals(BigDecimal value) {
		return getValueAsBigDecimal().compareTo(value) == 0;
	}

	public boolean isEmpty() {
		return value == null || value.trim().isEmpty();
	}

	@Override
	public String toString() {
		return "ValueEntryModel{" + "id='" + id + '\'' + ", namespace='" + namespace + '\'' + ", namespacePrefix='"
				+ namespacePrefix + '\'' + ", name='" + name + '\'' + ", value='" + value + '\'' + ", decimals='"
				+ decimals + '\'' + ", precision='" + precision + '\'' + ", contextId='" + contextId + '\''
				+ ", unitId='" + unitId + '\'' + '}';
	}

	public static class Builder {

		private String id;

		private String namespace;
		private String namespacePrefix;
		private String name;

		private String contextId;
		private String unitId;
		private Integer decimals;
		private String precision;
		private String value;

		public Builder setId(String id) {
			this.id = id;

			return this;
		}

		public Builder setNamespace(String namespace) {
			this.namespace = namespace;

			return this;
		}

		public Builder setNamespacePrefix(String namespacePrefix) {
			this.namespacePrefix = namespacePrefix;

			return this;
		}

		public Builder setName(String name) {
			this.name = name;

			return this;
		}

		public Builder setContext(XbrlContext context) {
			this.contextId = context.getId();

			return this;
		}

		public Builder setContextId(String contextId) {
			this.contextId = contextId;

			return this;
		}

		public Builder setUnit(XbrlUnit unit) {
			this.unitId = unit.getId();

			return this;
		}

		public Builder setUnitId(String unitId) {
			this.unitId = unitId;

			return this;
		}

		public Builder setDecimals(Integer decimals) {
			this.decimals = decimals;

			return this;
		}

		public Builder setPrecision(String precision) {
			this.precision = precision;

			return this;
		}

		public Builder setValue(String value) {
			this.value = value;

			return this;
		}

		public String buildId() {
			StringBuilder id = new StringBuilder();

			id.append(contextId);
			id.append(XbrlMetamodel.PARTS_DELIMITER);
			id.append(namespacePrefix);
			id.append(XbrlMetamodel.PREFIX_DELIMITER);
			id.append(name);

			return id.toString();
		}

		public XbrlValueEntry build() {
			// Check for required fields
			if (namespace == null || name == null) {
				throw new IllegalStateException("Some mandatory fields are missing.");
			}

			XbrlValueEntry metamodel = new XbrlValueEntry();
			metamodel.namespace = namespace;
			metamodel.namespacePrefix = namespacePrefix;
			metamodel.name = name;
			metamodel.contextId = contextId;
			metamodel.unitId = unitId;
			metamodel.value = value;
			metamodel.precision = precision;
			metamodel.decimals = decimals;

			// If id is not set, then generate an id
			if (id == null) {
				id = buildId();
			}

			metamodel.id = id;

			return metamodel;
		}
	}

}
