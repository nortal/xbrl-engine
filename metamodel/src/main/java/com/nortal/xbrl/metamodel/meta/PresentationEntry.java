package com.nortal.xbrl.metamodel.meta;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement(name = "presentation")
@XmlType(propOrder = { "labels", "ancestors", "references", "children" })
public class PresentationEntry extends AbstractReportEntry implements LinkedEntry, Serializable {
	private static final long serialVersionUID = 7724594809627470449L;

	public enum PeriodType {
		DURATION("duration"), INSTANT("instant"), FOREVER("forever");

		String name;

		PeriodType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static PeriodType fromString(String type) {
			for (PeriodType enumType : values()) {
				if (enumType.getName().equals(type)) {
					return enumType;
				}
			}
			throw new IllegalArgumentException("Unable to resolve period type for: " + type);
		}
	}

	public enum PresentationType {
		STRING("stringItemType"),
		SHARES("sharesItemType"),
		PER_SHARE("perShareItemType"),
		TEXT_BLOCK("textBlockItemType"),
		MONETARY("monetaryItemType"),
		DECIMAL("decimalItemType"),
		FLOAT("floatItemType"),
		DOUBLE("doubleItemType"),
		PERCENT("percentItemType"),
		FRACTION("fractionItemType"),
		PURE("pureItemType"),
		INTEGER("integerItemType"),
		POSITIVE_INTEGER("positiveIntegerItemType"),
		NON_POSITIVE_INTEGER("nonPositiveIntegerItemType"),
		NEGATIVE_INTEGER("negativeIntegerItemType"),
		NON_NEGATIVE_INTEGER("nonNegativeIntegerItemType"),
		BOOLEAN("booleanItemType"),
		BYTE("byteItemType"),
		U_BYTE("unsignedByteItemType"),
		SHORT("shortItemType"),
		U_SHORT("unsignedShortItemType"),
		INT("unsignedIntItemType"),
		U_INT("intItemType"),
		LONG("unsignedLongItemType"),
		U_LONG("longItemType"),
		DATE("dateItemType"),
		AXIS("dimensionItem"),
		DOMAIN("domainItemType"),
		TABLE("hypercubeItem");

		String name;

		PresentationType(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static PresentationType fromString(String type) {
			for (PresentationType enumType : values()) {
				if (enumType.getName().equals(type)) {
					return enumType;
				}
			}
			throw new IllegalArgumentException("Unable to resolve presentation type for: " + type);
		}
	}

	private PeriodType period;
	private PresentationType type;
	private boolean isAbstract;

	private Map<String, LabelEntry> labels;
	private List<String> ancestors;
	private List<PresentationEntry> children;
	private List<Reference> references;
	private LabelEntry.LabelType preferredLabelType;

	public PresentationEntry() {
		super();
		references = new ArrayList<Reference>();
		children = new ArrayList<PresentationEntry>() {

            private static final long serialVersionUID = 1564242772136265681L;

            @Override
			public boolean add(PresentationEntry element) {
				if (!super.add(element)) {
					return false;
				}

				for (int i = size() - 1; i > 0 && element.getOrder().compareTo(get(i - 1).getOrder()) < 0; i--) {
					Collections.swap(this, i, i - 1);
				}

				return true;
			}
		};
		labels = new HashMap<String, LabelEntry>();
	}

	@XmlAttribute(name = "type")
	public PresentationType getType() {
		return type;
	}

	public void setType(PresentationType type) {
		this.type = type;
	}

	@XmlAttribute(name = "period")
	public PeriodType getPeriod() {
		return period;
	}

	public void setPeriod(PeriodType period) {
		this.period = period;
	}

	@XmlAttribute(name = "abstract")
	public boolean isAbstract() {
		return isAbstract;
	}

	public void setAbstract(boolean isAbstract) {
		this.isAbstract = isAbstract;
	}

	@XmlElement
	@XmlJavaTypeAdapter(LabelMapAdapter.class)
	public Map<String, LabelEntry> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, LabelEntry> labels) {
		this.labels = labels;
	}

	@XmlElementWrapper(name = "ancestors")
	@XmlElement(name = "entry")
	public List<String> getAncestors() {
		return ancestors;
	}

	public void setAncestors(List<String> ancestors) {
		this.ancestors = ancestors;
	}

	@XmlElementWrapper(name = "children")
	@XmlElement(name = "entry")
	public List<PresentationEntry> getChildren() {
		return children;
	}

	public void setChildren(List<PresentationEntry> children) {
		this.children = children;
	}

	@XmlAttribute(name = "preferredLabelType")
	public LabelEntry.LabelType getPreferredLabelType() {
		return preferredLabelType;
	}

	public void setPreferredLabelType(LabelEntry.LabelType preferredLabelType) {
		this.preferredLabelType = preferredLabelType;
	}

	@XmlElementWrapper(name = "references")
	@XmlElement(name = "entry")
	public List<Reference> getReferences() {
		return references;
	}

	public void setReferences(List<Reference> references) {
		this.references = references;
	}

	public String getPreferredLabel(String locale) {
		if (!labels.containsKey(locale)) {
			return null;
		}

		if (preferredLabelType != null) {
			return labels.get(locale).getLabels().get(preferredLabelType);
		}
		else {
			return labels.get(locale).getDefaultLabel();
		}
	}

	public boolean isFlat() {
		for (PresentationEntry child : children) {
			if (child.isHypercube() || !child.isFlat()) {
				return false;
			}
		}

		return !isHypercube();
	}

	public boolean isHypercube() {
		for (PresentationEntry child : children) {
			if (child.getType().equals(PresentationType.TABLE)) {
				return true;
			}
		}

		return false;
	}

	public LinkedEntry getChild(LinkedEntry linkedEntry) {
		for (PresentationEntry presentationEntry : children) {
			if (presentationEntry.isLinked(linkedEntry)) {
				return presentationEntry;
			}
		}

		return null;
	}

	public boolean isLinked(LinkedEntry linkedEntry) {
		return getNamespace().equals(linkedEntry.getNamespace()) && getName().equals(linkedEntry.getName());
	}

}