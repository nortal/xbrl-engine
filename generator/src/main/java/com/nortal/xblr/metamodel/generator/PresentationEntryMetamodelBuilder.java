package com.nortal.xblr.metamodel.generator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xbrlapi.Arc;
import org.xbrlapi.Concept;
import org.xbrlapi.LabelResource;
import org.xbrlapi.Locator;
import org.xbrlapi.ReferencePart;
import org.xbrlapi.ReferenceResource;
import org.xbrlapi.utilities.Constants;
import org.xbrlapi.utilities.XBRLException;

import com.nortal.xbrl.metamodel.meta.LabelEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.Reference;

public class PresentationEntryMetamodelBuilder {

	private final Logger logger = Logger.getLogger(ReportingFormMetamodelBuilder.class);

	private final Locator locator;
	private final int level;
	private final boolean buildLabelsAndResources;

	public PresentationEntryMetamodelBuilder(Locator locator, int level, boolean buildLabelsAndResources) {
		this.locator = locator;
		this.level = level;
		this.buildLabelsAndResources = buildLabelsAndResources;
	}

	public PresentationEntry build() throws XBRLException {
		Concept concept = (Concept) locator.getTarget();

		logger.info(level + ": Building presentation " + concept.getName());

		PresentationEntry entry = new PresentationEntry();
		entry.setName(concept.getName());
		entry.setNamespace(concept.getTargetNamespace());
		entry.setAbstract(concept.isAbstract());
		entry.setLevel(level);
		entry.setPeriod(PresentationEntry.PeriodType.fromString(concept.getPeriodType()));

		if (!concept.getSubstitutionGroupLocalname().equals("item")) {
			entry.setType(PresentationEntry.PresentationType.fromString(concept
					.getSubstitutionGroupLocalname()));
		}
		else {
			entry.setType(PresentationEntry.PresentationType.fromString(concept.getTypeLocalname()));
		}

		if (buildLabelsAndResources) {
			populateLabelsAndResources(concept, entry);
		}

		logger.info("Fetching presentation children...");

		List<Arc> arcs = locator.getArcsFromWithArcrole(Constants.PresentationArcrole);
		for (Arc arc : arcs) {
			Locator targetLocator = MetamodelBuilderUtil.getTargetLocator(arc);
			PresentationEntryMetamodelBuilder presentationMetamodelBuilder = new PresentationEntryMetamodelBuilder(
					targetLocator, level + 1, buildLabelsAndResources);
			PresentationEntry child = presentationMetamodelBuilder.build();

			child.setOrder(arc.getOrder());

			if (arc.hasAttribute("preferredLabel")) {
				child.setPreferredLabelType(LabelEntry.LabelType.fromUri(arc.getAttribute("preferredLabel")));
			}

			entry.getChildren().add(child);
		}

		return entry;
	}

	private void populateLabelsAndResources(Concept concept, PresentationEntry entry) throws XBRLException {
		logger.debug("Fetching presentation labels...");

		List<LabelResource> labels = concept.getLabels();
		for (LabelResource label : labels) {
			LabelEntry labelsMetamodel = entry.getLabels().get(label.getLanguage());
			if (labelsMetamodel == null) {
				labelsMetamodel = new LabelEntry();
				entry.getLabels().put(label.getLanguage(), labelsMetamodel);
			}

			LabelEntry.LabelType labelType = LabelEntry.LabelType.fromUri(label.getResourceRole());
			// Remove anything inside [] and any leading whitespace character
			String labelStringValue = Pattern.compile("\\s?\\[.*\\]").matcher(label.getStringValue()).replaceAll("");
			labelsMetamodel.put(labelType, labelStringValue);
		}

		logger.debug("Fetching presentation reference...");

		List<ReferenceResource> references = concept.getReferences();
		for (ReferenceResource reference : references) {
			entry.getReferences().add(buildReferenceModel(reference));
		}
	}

	private Reference buildReferenceModel(ReferenceResource reference) throws XBRLException {
		Map<String, String> referenceData = new HashMap<>();
		List<ReferencePart> referenceParts = reference.getReferenceParts();
		for (ReferencePart referencePart : referenceParts) {
			referenceData.put(referencePart.getLocalname().toLowerCase(), referencePart.getValue());
		}
		return new Reference(reference.getLabel(), referenceData);
	}

}
