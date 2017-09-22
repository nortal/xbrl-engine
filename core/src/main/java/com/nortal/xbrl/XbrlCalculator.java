package com.nortal.xbrl;

import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;

/**
 * Xbrl calculator interface.
 */
public interface XbrlCalculator {

	/**
	 * Perform calculation based on reporting form calculation rules and store result in the instance model.
	 *
	 * @param report the reporting form
	 * @param instance the instance
	 * @return
	 */
	XbrlInstance calculate(ReportingFormMetamodel report, XbrlInstance instance);

}