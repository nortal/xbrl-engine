package com.nortal.xbrl;

import java.io.InputStream;
import java.util.Map;

import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;

/**
 * Xbrl mapper interface.
 */
public interface XbrlMapper {

	/**
	 * Create new jaxb element or class based on provided namespace and name.
	 *
	 * @param namespace element namespace
	 * @param name element name
	 * @return jaxb element or class
	 */
	Object createElement(String namespace, String name);

	/**
	 * Read meta model xml stream and return a list of report meta models.
	 *
	 * @param inputStream meta model xml input stream
	 * @return list of report meta models
	 */
	Map<String, ReportingFormMetamodel> getReportingFormMetamodels(InputStream inputStream);

	/**
	 * Get list of report meta models.
	 *
	 * @return list of report meta models
	 */
	Map<String, ReportingFormMetamodel> getReportMetamodels();

}
