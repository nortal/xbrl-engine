package com.nortal.xbrl;

import java.util.List;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlError;

/**
 * Xbrl validator interfrace.
 */
public interface XbrlValidator {

	/**
	 * Validate instance based on reporting form calculation rules.
	 * 
	 * @param report the reporting form
	 * @param instance the instance
	 * @return validation errors
	 */
	List<XbrlError> validate(ReportingFormMetamodel report, XbrlInstance instance, List<XbrlContext> xbrlContexts, String lang);

	/**
	 * Validate if reporting form contains data(either flat form or hypercybe).
	 *
	 * @param xbrlInstance the xbrlInstance
	 * @param xbrlContexts the xbrlContexts of reporting form
	 * @param reportingFormMetamodel the metamodel of reporting form
	 * @return true if reporting form contains data, otherwise false
	 */
	boolean formContainsData(XbrlInstance xbrlInstance, List<XbrlContext> xbrlContexts, ReportingFormMetamodel reportingFormMetamodel);
}
