package com.nortal.xbrl.metamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.metamodel.namespace.XbrlNamespaceMapping;

public class XbrlFieldValue implements Serializable {
	private static final long serialVersionUID = 5911412026817295052L;

	public enum ValueType {
		TEXT, PERSHARE,  MONETARY;
	}

	private Date startDate;
	private Date endDate;

	private String name;

	private String value;
	private ValueType valueType;

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public ValueType getValueType() {
		return valueType;
	}

	public String getValueNamespace() {
		String namePart = name.substring(name.lastIndexOf(XbrlMetamodel.PARTS_DELIMITER) + 1);
		String[] parts = namePart.split(XbrlMetamodel.PREFIX_DELIMITER);
		return XbrlNamespaceMapping.getNamespaceURI(parts[0]);
	}

	public String getValueNamespacePrefix() {
		String namePart = name.substring(name.lastIndexOf(XbrlMetamodel.PARTS_DELIMITER) + 1);
		String[] parts = namePart.split(XbrlMetamodel.PREFIX_DELIMITER);
		return parts[0];
	}

	public String getValueName() {
		String namePart = name.substring(name.lastIndexOf(XbrlMetamodel.PARTS_DELIMITER) + 1);
		String[] parts = namePart.split(XbrlMetamodel.PREFIX_DELIMITER);
		return parts[1];
	}

	public boolean hasExplicitMembers() {
		return name.contains(XbrlMetamodel.PARTS_DELIMITER);
	}

	public List<XbrlExplicitMember> getExplicitMembers() {
		if (!name.contains(XbrlMetamodel.PARTS_DELIMITER)) {
			return Collections.emptyList();
		}

		List<XbrlExplicitMember> xbrlExplicitMembers = new ArrayList<XbrlExplicitMember>();
		String explicitMembers = name.substring(0, name.lastIndexOf(XbrlMetamodel.PARTS_DELIMITER));

		String[] parts = explicitMembers.split(XbrlMetamodel.PARTS_DELIMITER);

		for (int i = 0; i < parts.length; i += 2) {
			// Extract dimension
			String dimension = parts[i];
			String dimensionNamespacePrefix = dimension.substring(0, dimension.indexOf(XbrlMetamodel.PREFIX_DELIMITER));
			String dimensionNamespace = XbrlNamespaceMapping.getNamespaceURI(dimensionNamespacePrefix);
			String dimensionName = dimension.substring(dimension.indexOf(XbrlMetamodel.PREFIX_DELIMITER) + 1);

			// Extract dimension value
			String value = parts[i + 1];
			String valueNamespacePrefix = value.substring(0, dimension.indexOf(XbrlMetamodel.PREFIX_DELIMITER));
			String valueNamespace = XbrlNamespaceMapping.getNamespaceURI(valueNamespacePrefix);
			String valueName = value.substring(dimension.indexOf(XbrlMetamodel.PREFIX_DELIMITER) + 1);

			xbrlExplicitMembers.add(new XbrlExplicitMember.Builder()
					.setDimension(dimensionNamespace, dimensionNamespacePrefix, dimensionName)
					.setValue(valueNamespace, valueNamespacePrefix, valueName).build());
		}

		return xbrlExplicitMembers;
	}

	public static class Builder {

		private Date startDate;
		private Date endDate;

		private String name;

		private String value;
		private ValueType valueType;

		public Builder setStartDate(Date startDate) {
			this.startDate = startDate;

			return this;
		}

		public Builder setEndDate(Date endDate) {
			this.endDate = endDate;

			return this;
		}

		public Builder setName(String name) {
			this.name = name;

			return this;
		}

		public Builder setValue(String value) {
			this.value = value;

			return this;
		}

		public Builder setValueType(ValueType valueType) {
			this.valueType = valueType;

			return this;
		}

		public XbrlFieldValue build() {
			XbrlFieldValue fieldValue = new XbrlFieldValue();

			fieldValue.startDate = startDate;
			fieldValue.endDate = endDate;
			fieldValue.name = name;
			fieldValue.value = value;
			fieldValue.valueType = valueType;

			return fieldValue;
		}

	}

}
