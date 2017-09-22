package com.nortal.xblr.metamodel.generator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.xbrlapi.data.Store;
import org.xbrlapi.utilities.Constants;

import com.nortal.xbrl.metamodel.meta.PresentationEntry;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xblr.metamodel.generator.store.StoreLocal;
import org.xbrlapi.utilities.XBRLException;

public class MetamodelBuilder {

	private final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
	private final Logger logger = Logger.getLogger(MetamodelBuilder.class);
	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private final String entryPoint;
	private final StoreLocal storeLocal;
	private final Store store;
	private final boolean buildLabelsAndResources;

	public MetamodelBuilder(String entryPoint, boolean buildLabelsAndResources) {
		this.entryPoint = entryPoint;
		this.buildLabelsAndResources = buildLabelsAndResources;
		this.storeLocal = new StoreLocal(entryPoint);
		this.store = storeLocal.get();
	}

	public XbrlMetamodel build() throws Exception {
		Set<String> linkRoles = store.getLinkRoles(Constants.PresentationArcrole);
		logger.debug("Total number of roles: " + linkRoles.size()); // Expecting 10

		XbrlMetamodel xbrlMetamodel = new XbrlMetamodel();
		xbrlMetamodel.setReports(buildReportingFormMetamodels(linkRoles));
		xbrlMetamodel.setTaxonomyVersionDate(getTaxonomyVersion());

		return xbrlMetamodel;
	}

	public void dispose() {
		storeLocal.release();
	}

	private Date getTaxonomyVersion() throws ParseException {
		Matcher matcher = DATE_PATTERN.matcher(entryPoint);

		if (matcher.find()) {
			return DATE_FORMAT.parse(matcher.group());
		}

		// Try to find the taxonomy version from the target namespace url
		try {
			String targetNamespace = store.getStoreAsDOM()
					.getElementsByTagName("xsd:schema")
					.item(0)
					.getAttributes()
					.getNamedItem("targetNamespace")
					.getNodeValue();
			matcher = DATE_PATTERN.matcher(targetNamespace);

			if (matcher.find()) {
				return DATE_FORMAT.parse(matcher.group());
			}
		} catch (XBRLException e) {
			logger.error("Could not extract taxonomy version from schema definition", e);
		}

        logger.info("Taxonomy version date is not found, use current date");

		return new Date();
	}

	private List<ReportingFormMetamodel> buildReportingFormMetamodels(Set<String> linkRoles) throws Exception {
		final List<ReportingFormMetamodel> forms = new ArrayList<>();
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		// ExecutorService executorService = Executors.newSingleThreadExecutor();
		for (final String linkRole : linkRoles) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					ReportingFormMetamodelBuilder formMetamodelBuilder = new ReportingFormMetamodelBuilder(storeLocal,
							linkRole, buildLabelsAndResources);
					try {
						forms.add(formMetamodelBuilder.build());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
					finally {
						formMetamodelBuilder.dispose();
					}
				}
			});
		}
		executorService.shutdown();
		executorService.awaitTermination(1, TimeUnit.DAYS);
		populateFieldAncestors(forms);
		Collections.sort(forms);
		return forms;
	}

	// TODO: Refactor
	private void populateFieldAncestors(List<ReportingFormMetamodel> forms) {
		Map<String, List<String>> fieldAncestorses = new HashMap<>();
		Set<PresentationEntry> allFields = new HashSet<>();
		for (ReportingFormMetamodel form : forms) {
			if (!form.getHypercubePresentation().isEmpty()) {
				continue;
			}
			for (PresentationEntry presentationEntry : getFields(form)) {
				String fieldName = presentationEntry.getName();
				allFields.add(presentationEntry);
				List<String> curFieldInForms = fieldAncestorses.get(fieldName);
				if (curFieldInForms == null) {
					curFieldInForms = new ArrayList<>();
					fieldAncestorses.put(fieldName, curFieldInForms);
				}
				curFieldInForms.add(form.getCode());
			}
		}
		for (List<String> fieldAncestors : fieldAncestorses.values()) {
			Collections.sort(fieldAncestors);
		}
		for (PresentationEntry field : allFields) {
			field.setAncestors(fieldAncestorses.get(field.getName()));
		}
	}

	private Set<PresentationEntry> getFields(ReportingFormMetamodel reportingForm) {
		Set<PresentationEntry> fields = new HashSet<>();
		Stack<PresentationEntry> traverseStack = new Stack<>();
		traverseStack.addAll(reportingForm.getPresentation());
		while (!traverseStack.empty()) {
			PresentationEntry field = traverseStack.pop();
			fields.add(field);
			traverseStack.addAll(field.getChildren());
		}
		return fields;
	}

}
