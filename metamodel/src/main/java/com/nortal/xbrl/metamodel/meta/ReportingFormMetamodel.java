package com.nortal.xbrl.metamodel.meta;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.nortal.xbrl.metamodel.XbrlFieldValue;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry.PresentationType;

@XmlRootElement(name = "form")
@XmlType(propOrder = { "name", "presentation", "calculation", "dimension" })
public class ReportingFormMetamodel implements Serializable, Comparable<ReportingFormMetamodel> {
	private static final long serialVersionUID = -3391057394636083419L;

	private Map<String, String> name;
	private String code;
	private String linkRoleUri;
	private List<PresentationEntry> presentation;
	private List<CalculationEntry> calculation;
	private List<DimensionEntry> dimension;

	public ReportingFormMetamodel() {
		name = new HashMap<String, String>();
		presentation = new ArrayList<PresentationEntry>();
		calculation = new ArrayList<CalculationEntry>();
		dimension = new ArrayList<DimensionEntry>();
	}

	@XmlElement
	@XmlJavaTypeAdapter(StringMapAdapter.class)
	public Map<String, String> getName() {
		return name;
	}

	public void setName(Map<String, String> name) {
		this.name = name;
	}

	@XmlAttribute(name = "code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@XmlAttribute(name = "period")
	public PresentationEntry.PeriodType getPeriod() {
		List<PresentationEntry> presentationEntries = getFlatPresentation();
		for (PresentationEntry presentationEntry : presentationEntries) {
			if (presentationEntry.getPeriod().equals(PresentationEntry.PeriodType.DURATION)
					&& !presentationEntry.isAbstract()) {
				return PresentationEntry.PeriodType.DURATION;
			}
		}
		return PresentationEntry.PeriodType.INSTANT;
	}

	@XmlAttribute(name = "linkRoleUri")
	public String getLinkRoleUri() {
		return linkRoleUri;
	}

	public void setLinkRoleUri(String linkRoleUri) {
		this.linkRoleUri = linkRoleUri;
	}

	@XmlElementWrapper(name = "presentations")
	@XmlElement(name = "entry")
	public List<PresentationEntry> getPresentation() {
		return presentation;
	}

	public void setPresentation(List<PresentationEntry> presentation) {
		this.presentation = presentation;
	}

	@XmlElementWrapper(name = "calculations")
	@XmlElement(name = "entry")
	public List<CalculationEntry> getCalculation() {
		return calculation;
	}

	public void setCalculation(List<CalculationEntry> calculation) {
		this.calculation = calculation;
	}

	@XmlElementWrapper(name = "dimensions")
	@XmlElement(name = "entry")
	public List<DimensionEntry> getDimension() {
		return dimension;
	}

	public void setDimension(List<DimensionEntry> dimension) {
		this.dimension = dimension;
	}

	public DimensionEntry getDimension(LinkedEntry linkedEntry) {
		return (DimensionEntry) getLinkedEntry(linkedEntry, dimension);
	}

	public PresentationEntry getPresentation(LinkedEntry linkedEntry) {
		return (PresentationEntry) getLinkedEntry(linkedEntry, presentation);
	}

	public PresentationEntry getPresentation(XbrlFieldValue xbrlFieldValue) {
		PresentationEntry linkedEntry = createValueLinkedEntry(xbrlFieldValue.getName(), xbrlFieldValue.getValueNamespace());
		return linkedEntry == null ? linkedEntry : (PresentationEntry) getLinkedEntry(linkedEntry, presentation);
	}

	public PresentationEntry getPresentation(XbrlValueEntry xbrlValueEntry) {
		PresentationEntry linkedEntry = createValueLinkedEntry(xbrlValueEntry.getName(), xbrlValueEntry.getNamespace());
		return linkedEntry == null ? linkedEntry : (PresentationEntry) getLinkedEntry(linkedEntry, presentation);
	}
	
	private PresentationEntry createValueLinkedEntry(String name, String namespace) {
		if (name == null) {
			return null;
		}
		PresentationEntry linkedEntry = new PresentationEntry();
		linkedEntry.setName(name.substring(name.lastIndexOf(XbrlMetamodel.PREFIX_DELIMITER) + 1));
		linkedEntry.setNamespace(namespace);
		return linkedEntry;
	}

	public CalculationEntry getCalculation(LinkedEntry linkedEntry) {
		return (CalculationEntry) getLinkedEntry(linkedEntry, calculation);
	}

	protected <T extends LinkedEntry> LinkedEntry getLinkedEntry(LinkedEntry linkedEntry, List<T> entries) {
		for (T entry : entries) {
			if (entry.isLinked(linkedEntry)) {
				return entry;
			}

			if (!entry.getChildren().isEmpty()) {
				LinkedEntry childLinkedEntry = getLinkedEntry(linkedEntry, entry.getChildren());

				if (childLinkedEntry != null) {
					return childLinkedEntry;
				}
			}
		}

		return null;
	}

	public boolean isTotalPresentation(PresentationEntry presentation) {
		CalculationEntry calculation = getCalculation(presentation);
		return calculation != null && !calculation.getChildren().isEmpty();
	}

	public List<PresentationEntry> getFlatPresentation() {
		return getFlatPresentation(presentation);
	}

	protected List<PresentationEntry> getFlatPresentation(List<PresentationEntry> presentations) {
		List<PresentationEntry> list = new ArrayList<PresentationEntry>();

		for (PresentationEntry presentation : presentations) {
			if (presentation.isHypercube()) {
				continue;
			}

			if (presentation.getLevel() != 0) {
				list.add(presentation);
			}

			if (!presentation.getChildren().isEmpty()) {
				list.addAll(getFlatPresentation(presentation.getChildren()));
			}
		}

		return list;
	}

	public List<PresentationEntry> getHypercubePresentation() {
		return getHypercubePresentation(presentation);
	}

	protected List<PresentationEntry> getHypercubePresentation(List<PresentationEntry> presentations) {
		List<PresentationEntry> list = new ArrayList<PresentationEntry>();

		for (PresentationEntry presentation : presentations) {
			if (presentation.isHypercube()) {
				list.add(presentation);
			}

			if (!presentation.getChildren().isEmpty()) {
				list.addAll(getHypercubePresentation(presentation.getChildren()));
			}
		}

		return list;
	}

	@Override
	public String toString() {
		String br = System.getProperty("line.separator");
		return name + br + "Presentation: " + br + recursePresentationMetamodels("", new StringBuilder(), presentation)
				+ br + "Calculation: " + br + recurseCalculationMetamodels("", new StringBuilder(), calculation);
	}

	private StringBuilder recursePresentationMetamodels(String indent, StringBuilder result,
			List<PresentationEntry> entries) {
		for (PresentationEntry entry : entries) {
			result.append(indent + entry.getName());
			result.append(" ");
			result.append(entry.getLabels());
			result.append(System.getProperty("line.separator"));
			result.append(recursePresentationMetamodels(indent + " ", new StringBuilder(), entry.getChildren()));
		}
		return result;
	}

	private StringBuilder recurseCalculationMetamodels(String indent, StringBuilder result,
			List<CalculationEntry> entries) {
		for (CalculationEntry entry : entries) {
			result.append(indent + entry.getName());
			result.append(" ");
			result.append(entry.getBalance());
			result.append(System.getProperty("line.separator"));
			result.append(recurseCalculationMetamodels(indent + " ", new StringBuilder(), entry.getChildren()));
		}
		return result;
	}

	@Override
	public int compareTo(ReportingFormMetamodel otherReportingFormMetamodel) {
		return getCode().compareTo(otherReportingFormMetamodel.getCode());
	}

	public boolean hasOnlyTextFields() {
		for (PresentationEntry flatPresentation : getFlatPresentation()) {
			if (!flatPresentation.isAbstract()) {
				if (flatPresentation.getType() != PresentationType.TEXT_BLOCK) {
					return false;
				}
			}
		}
		return true;
	}

}
