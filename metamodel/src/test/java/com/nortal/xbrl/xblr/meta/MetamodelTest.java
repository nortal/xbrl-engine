package com.nortal.xbrl.xblr.meta;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import org.junit.Test;

import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;

/**
 * Unit test for simple App.
 */
public class MetamodelTest {

	private final static String CONTEXT_ID = "CONTEXT_ID";
	private final static String CONTEXT_SCHEMA = "CONTEXT_SCHEMA";
	private final static String CONTEXT_NAME = "CONTEXT_NAME";

	private final static String UNIT_ID = "UNIT_ID";
	private final static String UNIT_MEASURE = "UNIT_MEASURE";
	private final static String UNIT_NUMERATOR_MEASURE = "UNIT_NUMERATOR_MEASURE";
	private final static String UNIT_DENOMINATOR_MEASURE = "UNIT_DENOMINATOR_MEASURE";

	private final static String VALUE_ID = "VALUE_ID";
	private final static String VALUE_NAMESPACE = "VALUE_NAMESPACE";
	private final static String VALUE_NAMESPACE_PREFIX = "VALUE_NAMESPACE_PREFIX";
	private final static String VALUE_NAME = "VALUE_NAME";

	@Test
	public void testApp() {
		XbrlInstance report = new XbrlInstance();

		XbrlContext context = new XbrlContext.Builder(CONTEXT_ID, CONTEXT_SCHEMA, CONTEXT_NAME).build();

		XbrlUnit unit = new XbrlUnit.Builder(UNIT_ID, UNIT_MEASURE, UNIT_NUMERATOR_MEASURE, UNIT_DENOMINATOR_MEASURE).build();

		XbrlValueEntry value = new XbrlValueEntry.Builder().setId(VALUE_ID).setNamespace(VALUE_NAMESPACE)
				.setNamespacePrefix(VALUE_NAMESPACE_PREFIX).setName(VALUE_NAME).setContext(context).setUnit(unit)
				.build();

		report.addValue(value);
	}

}
