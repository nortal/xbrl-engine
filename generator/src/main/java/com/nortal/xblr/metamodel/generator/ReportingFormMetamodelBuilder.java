package com.nortal.xblr.metamodel.generator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nortal.xblr.metamodel.generator.store.StoreLocal;
import org.apache.log4j.Logger;
import org.xbrlapi.Arc;
import org.xbrlapi.Concept;
import org.xbrlapi.LabelResource;
import org.xbrlapi.Locator;
import org.xbrlapi.RoleType;
import org.xbrlapi.data.Store;
import org.xbrlapi.utilities.Constants;
import org.xbrlapi.utilities.XBRLException;
import org.xbrlapi.xdt.XDTConstants;

import com.nortal.xbrl.metamodel.meta.CalculationEntry;
import com.nortal.xbrl.metamodel.meta.DimensionEntry;
import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;

public class ReportingFormMetamodelBuilder {

	private final Logger logger = Logger.getLogger(ReportingFormMetamodelBuilder.class);
	private final Pattern FORM_CODE_PATTERN = Pattern.compile("\\[(\\d{6})\\]");

	private final StoreLocal storeLocal;
	private final Store store;
	private final String linkRole;

	private final boolean buildLabelsAndResources;

	public ReportingFormMetamodelBuilder(StoreLocal storeLocal, String linkRole, boolean buildLabelsAndResources) {
		this.storeLocal = storeLocal;
		this.linkRole = linkRole;
		this.buildLabelsAndResources = buildLabelsAndResources;
		this.store = storeLocal.get();
	}

	public ReportingFormMetamodel build() throws Exception {
		logger.debug("Building model for role " + linkRole);
		ReportingFormMetamodel form = new ReportingFormMetamodel();

		RoleType roleType = getRoleType();
		form.setLinkRoleUri(linkRole);

		if (buildLabelsAndResources) {
			populateLabelsAndResources(form, roleType);
		}

		form.setCode(findFormCode(roleType));
		form.getPresentation().addAll(getPresentations());
		form.getCalculation().addAll(getCalculations());
		form.getDimension().addAll(getDimensions());

		return form;
	}

	public void dispose() {
		storeLocal.release();
	}

	private List<DimensionEntry> getDimensions() throws XBRLException {
		logger.debug("Fetching dimensions..."); // TODO: Better log message
		Set<Locator> dimensionLocators = store.getNetworkRoots(linkRole, XDTConstants.AllArcrole);
		List<DimensionEntry> dimensions = new ArrayList<>();
		for (Locator locator : dimensionLocators) {
			dimensions.add(buildDimensionModel(locator, 0));
		}
		return dimensions;
	}

	private List<CalculationEntry> getCalculations() throws XBRLException {
		logger.debug("Fetching calculations..."); // TODO: Better log message
		Set<Locator> calculationLocators = store.getNetworkRoots(linkRole, Constants.CalculationArcrole);
		List<CalculationEntry> calculations = new ArrayList<>();
		for (Locator locator : calculationLocators) {
			calculations.add(buildCalculationModel(locator, 0));
		}
		return calculations;
	}

	private List<PresentationEntry> getPresentations() throws XBRLException {
		logger.debug("Fetching presentations..."); // TODO: Better log message
		Set<Locator> presentationLocators = store.getNetworkRoots(linkRole, Constants.PresentationArcrole);
		List<PresentationEntry> presentations = new ArrayList<>();
		for (Locator locator : presentationLocators) {
			PresentationEntryMetamodelBuilder presentationMetamodelBuilder = new PresentationEntryMetamodelBuilder(
					locator, 0, buildLabelsAndResources);
			presentations.add(presentationMetamodelBuilder.build());
		}
		return presentations;
	}

	private String findFormCode(RoleType roleType) throws XBRLException {
		Matcher matcher = FORM_CODE_PATTERN.matcher(roleType.getDefinition());
		if (!matcher.find()) {
			throw new IllegalStateException("Unable to find code for reporting form " + roleType.getDefinition());
		}
		return matcher.group(1);
	}

	private RoleType getRoleType() throws XBRLException {
		List<RoleType> roleTypes = store.getRoleTypes(linkRole);
		if (roleTypes.size() != 1) {
			throw new IllegalStateException("Number of roleTypes for linkRole " + linkRole + " should be 1, but found "
					+ roleTypes.size());
		}
		return roleTypes.get(0);
	}

	private void populateLabelsAndResources(ReportingFormMetamodel form, RoleType roleType) throws XBRLException {
		logger.debug("Fetching labels...");
		List<LabelResource> roleTypeLabels = roleType.getLabels();
		for (LabelResource label : roleTypeLabels) {
			Matcher matcher = Pattern.compile("\\[(\\d{6})\\s*\\]\\s+(.+)").matcher(label.getStringValue());
			String name = matcher.find() ? matcher.group(2) : label.getStringValue();
			form.getName().put(label.getLanguage(), name);
		}
	}

	private CalculationEntry buildCalculationModel(Locator locator, int level) throws XBRLException {
		Concept concept = (Concept) locator.getTarget();

		CalculationEntry calculation = new CalculationEntry();
		calculation.setName(concept.getName());
		calculation.setNamespace(concept.getTargetNamespace());
		calculation.setLevel(level);
		calculation.setBalance(CalculationEntry.Balance.fromString(concept.getBalance()));

		level++;

		logger.debug("Fetching calculation children..."); // TODO: Better log message

		List<Arc> arcs = locator.getArcsFromWithArcrole(Constants.CalculationArcrole);
		for (Arc arc : arcs) {
			Locator targetLocator = MetamodelBuilderUtil.getTargetLocator(arc);
			CalculationEntry child = buildCalculationModel(targetLocator, level);

			child.setOrder(arc.getOrder());
			child.setWeight(BigDecimal.valueOf(arc.getWeight()));

			calculation.getChildren().add(child);
		}

		return calculation;
	}

	private DimensionEntry buildDimensionModel(Locator locator, int level) throws XBRLException {
		Concept concept = (Concept) locator.getTarget();

		DimensionEntry entry = new DimensionEntry();
		entry.setName(concept.getName());
		entry.setNamespace(concept.getTargetNamespace());
		entry.setLevel(level);
		entry.setAbstract(concept.isAbstract());

		level++;

		buildDimensionModelChildren(entry, locator, level, XDTConstants.AllArcrole);
		buildDimensionModelChildren(entry, locator, level, XDTConstants.HypercubeDimensionArcrole);
		buildDimensionModelChildren(entry, locator, level, XDTConstants.DimensionDomainArcrole);
		buildDimensionModelChildren(entry, locator, level, XDTConstants.DomainMemberArcrole);

		return entry;
	}

	private void buildDimensionModelChildren(DimensionEntry entry, Locator locator, int level, String arcRole)
			throws XBRLException {
		// Read arc roles - domains
		for (Arc arc : locator.getArcsFromWithArcrole(arcRole)) {
			Locator targetLocator = MetamodelBuilderUtil.getTargetLocator(arc);
			DimensionEntry child = buildDimensionModel(targetLocator, level);
			child.setArcRole(DimensionEntry.ArcRole.fromString(arc.getArcrole()));

			// has-hypercube arcs
			if (arc.hasAttribute(XDTConstants.XBRLDTNamespace, "contextElement")) {
				child.setContextElement(arc.getAttribute(XDTConstants.XBRLDTNamespace, "contextElement"));
			}

			// has-hypercube arcs
			if (arc.hasAttribute(XDTConstants.XBRLDTNamespace, "closed")) {
				child.setClosed(Boolean.parseBoolean(arc.getAttribute(XDTConstants.XBRLDTNamespace, "closed")));
			}

			// dimension-domain or domain-member
			if (arc.hasAttribute(XDTConstants.XBRLDTNamespace, "usable")) {
				child.setUsable(Boolean.parseBoolean(arc.getAttribute(XDTConstants.XBRLDTNamespace, "usable")));
			}

			entry.getChildren().add(child);
		}
	}

}
