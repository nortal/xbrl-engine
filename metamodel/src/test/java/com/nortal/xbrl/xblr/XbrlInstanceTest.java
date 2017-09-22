package com.nortal.xbrl.xblr;

import java.util.Date;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import org.junit.Assert;
import org.junit.Test;

import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;

/**
 * Unit test for simple App.
 */
public class XbrlInstanceTest {

	private final static String CONTEXT_ID = "CONTEXT_ID";
	private final static String CONTEXT_SCHEMA = "CONTEXT_SCHEMA";
	private final static String CONTEXT_NAME = "CONTEXT_NAME";

	private final static String UNIT_ID = "UNIT_ID";
	private final static String UNIT_MEASURE = "UNIT_MEASURE";
	private final static String UNIT_NUMERATOR_MEASURE = "UNIT_NUMERATOR_MEASURE";
	private final static String UNIT_DENOMINATOR_MEASURE = "UNIT_DENOMINATOR_MEASURE";

	private final static Date PERIOD_START_DATE = new Date();
	private final static Date PERIOD_END_DATE = new Date();

	private final static String SCENARIO_DIMENSION_NS = "SCENARIO_DIMENSION_NAMESPACE";
	private final static String SCENARIO_DIMENSION_NS_PREFIX = "SCENARIO_DIMENSION_NAMESPACE_PREFIX";
	private final static String SCENARIO_DIMENSION = "SCENARIO_DIMENSION";
	private final static String SCENARIO_NAME_NS = "SCENARIO_NAME_NAMESPACE";
	private final static String SCENARIO_NAME_NS_PREFIX = "SCENARIO_NAME_NAMESPACE_PREFIX";
	private final static String SCENARIO_NAME = "SCENARIO_NAME";

	private final static String VALUE_NAMESPACE = "VALUE_NAMESPACE";
	private final static String VALUE_NAMESPACE_PREFIX = "VALUE_NAMESPACE_PREFIX";
	private final static String VALUE_NAME = "VALUE_NAME";

	@Test
	public void testReportInstanceMetaModel() {
		XbrlContext contextOne = new XbrlContext.Builder(CONTEXT_ID, CONTEXT_SCHEMA, CONTEXT_NAME)
				.setDateRange(PERIOD_START_DATE, PERIOD_END_DATE)
				.setScenario(
						new XbrlExplicitMember.Builder()
								.setDimension(SCENARIO_DIMENSION_NS, SCENARIO_DIMENSION_NS_PREFIX, SCENARIO_DIMENSION)
								.setValue(SCENARIO_NAME_NS, SCENARIO_NAME_NS_PREFIX, SCENARIO_NAME).build()).build();

		XbrlContext contextTwo = new XbrlContext.Builder(CONTEXT_ID + "_2", CONTEXT_SCHEMA + "_2", CONTEXT_NAME
				+ "_2")
				.setDateRange(PERIOD_START_DATE, PERIOD_END_DATE)
				.setScenario(
						new XbrlExplicitMember.Builder()
								.setDimension(SCENARIO_DIMENSION_NS, SCENARIO_DIMENSION_NS_PREFIX, SCENARIO_DIMENSION)
								.setValue(SCENARIO_NAME_NS, SCENARIO_NAME_NS_PREFIX, SCENARIO_NAME).build()).build();

		XbrlUnit unitOne = new XbrlUnit.Builder(UNIT_ID, UNIT_MEASURE, UNIT_NUMERATOR_MEASURE, UNIT_DENOMINATOR_MEASURE).build();

		XbrlUnit unitTwo = new XbrlUnit.Builder(UNIT_ID + "_2", UNIT_MEASURE, UNIT_NUMERATOR_MEASURE, UNIT_DENOMINATOR_MEASURE).build();

		XbrlValueEntry valueEntryOne = new XbrlValueEntry.Builder().setContext(contextOne).setUnit(unitOne)
				.setNamespace(VALUE_NAMESPACE).setNamespacePrefix(VALUE_NAMESPACE_PREFIX).setName(VALUE_NAME).build();

		XbrlValueEntry valueEntryTwo = new XbrlValueEntry.Builder().setContext(contextTwo).setUnit(unitTwo)
				.setNamespace(VALUE_NAMESPACE).setNamespacePrefix(VALUE_NAMESPACE_PREFIX).setName(VALUE_NAME).build();

		XbrlInstance xbrlInstance = new XbrlInstance();

		xbrlInstance.addContext(contextOne);
		xbrlInstance.addContext(contextTwo);

		xbrlInstance.addUnit(unitOne);
		xbrlInstance.addUnit(unitTwo);

		xbrlInstance.addValue(valueEntryOne);
		xbrlInstance.addValue(valueEntryTwo);

		Assert.assertEquals(2, xbrlInstance.getContexts().size());
		Assert.assertEquals(2, xbrlInstance.getUnits().size());
		Assert.assertEquals(2, xbrlInstance.getValues().size());
	}

}
