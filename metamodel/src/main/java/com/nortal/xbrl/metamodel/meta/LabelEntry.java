package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LabelEntry implements Serializable {

	private static final long serialVersionUID = -7141012779042613241L;

	public enum LabelType {
		DEFAULT("http://www.xbrl.org/2003/role/label"),
		DOCUMENTATION("http://www.xbrl.org/2003/role/documentation"),
		PERIOD_START("http://www.xbrl.org/2003/role/periodStartLabel"),
		PERIOD_END("http://www.xbrl.org/2003/role/periodEndLabel"),
		NET("http://www.xbrl.org/2009/role/netLabel"),
		TOTAL("http://www.xbrl.org/2003/role/totalLabel"),
		TERSE("http://www.xbrl.org/2003/role/terseLabel"),
		NEGATED_TERSE("http://www.xbrl.org/2009/role/negatedTerseLabel"),
		NEGATED("http://www.xbrl.org/2009/role/negatedLabel"),
		NEGATED_TOTAL("http://www.xbrl.org/2009/role/negatedTotalLabel");

		private String uri;

		LabelType(String uri) {
			this.uri = uri;
		}

		public String getUri() {
			return uri;
		}

		public static LabelType fromUri(String uri) {
			for (LabelType type : values()) {
				if (type.getUri().equals(uri)) {
					return type;
				}
			}
			throw new IllegalArgumentException("Unable to find label type for uri: " + uri);
		}
	}

	public LabelEntry() {
		labels = new HashMap<LabelType, String>();
	}

	private Map<LabelType, String> labels;

	public String getDefaultLabel() {
		return labels.get(LabelType.DEFAULT);
	}

	public String getDocumentationLabel() {
		return labels.get(LabelType.DOCUMENTATION);
	}

	public String getPeriodStartLabel() {
		return labels.get(LabelType.PERIOD_START);
	}

	public String getPeriodEndLabel() {
		return labels.get(LabelType.PERIOD_END);
	}

	public String getTotalLabel() {
		return labels.get(LabelType.TOTAL);
	}

	public String getNegatedLabel() {
		return labels.get(LabelType.NEGATED);
	}

	public String getNetLabel() {
		return labels.get(LabelType.NET);
	}

	public Map<LabelType, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<LabelType, String> labels) {
		this.labels = labels;
	}

	public void put(LabelType labelType, String value) {
		labels.put(labelType, value);
	}
}
