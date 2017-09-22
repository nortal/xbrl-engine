package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;

public class XbrlExplicitMember implements Serializable {

	private static final long serialVersionUID = -3254075385925497404L;

	private String id;

	private String dimensionNamespace;
	private String dimensionNamespacePrefix;
	private String dimensionName;

	private String valueNamespace;
	private String valueNamespacePrefix;
	private String valueName;

	public String getId() {
		return id;
	}

	public String getDimensionNamespace() {
		return dimensionNamespace;
	}

	public String getDimensionNamespacePrefix() {
		return dimensionNamespacePrefix;
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public String getValueNamespace() {
		return valueNamespace;
	}

	public String getValueNamespacePrefix() {
		return valueNamespacePrefix;
	}

	public String getValueName() {
		return valueName;
	}

	public static class Builder {

		private String id;

		private String dimensionNamespaceURI;
		private String dimensionNamespacePrefix;
		private String dimensionName;

		private String valueNamespaceURI;
		private String valueNamespacePrefix;
		private String valueName;

		public Builder() {

		}

		public Builder setId(String id) {
			this.id = id;

			return this;
		}

		public Builder setDimension(String namespaceURI, String namespacePrefix, String name) {
			this.dimensionNamespaceURI = namespaceURI;
			this.dimensionNamespacePrefix = namespacePrefix;
			this.dimensionName = name;

			return this;
		}

		public Builder setValue(String namespaceURI, String namespacePrefix, String name) {
			this.valueNamespaceURI = namespaceURI;
			this.valueNamespacePrefix = namespacePrefix;
			this.valueName = name;

			return this;
		}

		public String buildId() {
			StringBuilder id = new StringBuilder();

			id.append(dimensionNamespacePrefix);
			id.append(XbrlMetamodel.PREFIX_DELIMITER);
			id.append(dimensionName);
			id.append(XbrlMetamodel.PARTS_DELIMITER);
			id.append(valueNamespacePrefix);
			id.append(XbrlMetamodel.PREFIX_DELIMITER);
			id.append(valueName);

			return id.toString();
		}

		public XbrlExplicitMember build() {
			XbrlExplicitMember metamodel = new XbrlExplicitMember();

			metamodel.dimensionNamespace = dimensionNamespaceURI;
			metamodel.dimensionNamespacePrefix = dimensionNamespacePrefix;
			metamodel.dimensionName = dimensionName;
			metamodel.valueNamespace = valueNamespaceURI;
			metamodel.valueNamespacePrefix = valueNamespacePrefix;
			metamodel.valueName = valueName;

			// If id is not set, then generate an id
			if (id == null) {
				id = buildId();
			}

			metamodel.id = id;

			return metamodel;
		}

	}

}
