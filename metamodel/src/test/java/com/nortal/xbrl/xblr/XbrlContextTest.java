package com.nortal.xbrl.xblr;

import java.util.Date;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class XbrlContextTest {
	private final static String CONTEXT_ID = "CONTEXT_ID";
	private final static String CONTEXT_SCHEMA = "CONTEXT_SCHEMA";
	private final static String CONTEXT_NAME = "CONTEXT_NAME";

	private final static Date PERIOD_START_DATE = new Date();
	private final static Date PERIOD_END_DATE = new Date();

	private final static String SEGMENT_DIMENSION_NS = "SEGMENT_DIMENSION_NAMESPACE";
	private final static String SEGMENT_DIMENSION_NS_PREFIX = "SEGMENT_DIMENSION_NAMESPACE_PREFIX";
	private final static String SEGMENT_DIMENSION = "SEGMENT_DIMENSION";
	private final static String SEGMENT_NAME_NS = "SEGMENT_NAME_NAMESPACE";
	private final static String SEGMENT_NAME_NS_PREFIX = "SEGMENT_NAME_NAMESPACE_PREFIX";
	private final static String SEGMENT_NAME = "SEGMENT_NAME";

	private final static String SCENARIO_DIMENSION_NS = "SCENARIO_DIMENSION_NAMESPACE";
	private final static String SCENARIO_DIMENSION_NS_PREFIX = "SCENARIO_DIMENSION_NAMESPACE_PREFIX";
	private final static String SCENARIO_DIMENSION = "SCENARIO_DIMENSION";
	private final static String SCENARIO_NAME_NS = "SCENARIO_NAME_NAMESPACE";
	private final static String SCENARIO_NAME_NS_PREFIX = "SCENARIO_NAME_NAMESPACE_PREFIX";
	private final static String SCENARIO_NAME = "SCENARIO_NAME";

	@Test
	public void testContextMetamodelBuilder() {
		XbrlContext metamodel = new XbrlContext.Builder(CONTEXT_ID, CONTEXT_SCHEMA, CONTEXT_NAME)
				.setDateRange(PERIOD_START_DATE, PERIOD_END_DATE)
				.setSegment(
						new XbrlExplicitMember.Builder()
								.setDimension(SEGMENT_DIMENSION_NS, SEGMENT_DIMENSION_NS_PREFIX, SEGMENT_DIMENSION)
								.setValue(SEGMENT_NAME_NS, SEGMENT_NAME_NS_PREFIX, SEGMENT_NAME).build())
				.setScenario(
						new XbrlExplicitMember.Builder()
								.setDimension(SCENARIO_DIMENSION_NS, SCENARIO_DIMENSION_NS_PREFIX, SCENARIO_DIMENSION)
								.setValue(SCENARIO_NAME_NS, SCENARIO_NAME_NS_PREFIX, SCENARIO_NAME).build()).build();

		Assert.assertEquals(CONTEXT_ID, metamodel.getId());
		Assert.assertEquals(CONTEXT_SCHEMA, metamodel.getEntityScheme());
		Assert.assertEquals(CONTEXT_NAME, metamodel.getEntityName());
		Assert.assertEquals(PERIOD_START_DATE, metamodel.getPeriodStartDate());
		Assert.assertEquals(PERIOD_END_DATE, metamodel.getPeriodEndDate());
		Assert.assertEquals(SEGMENT_DIMENSION_NS, metamodel.getSegments().get(0).getDimensionNamespace());
		Assert.assertEquals(SEGMENT_DIMENSION_NS_PREFIX, metamodel.getSegments().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SEGMENT_DIMENSION, metamodel.getSegments().get(0).getDimensionName());
		Assert.assertEquals(SEGMENT_NAME_NS, metamodel.getSegments().get(0).getValueNamespace());
		Assert.assertEquals(SEGMENT_NAME_NS_PREFIX, metamodel.getSegments().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SEGMENT_NAME, metamodel.getSegments().get(0).getValueName());
		Assert.assertEquals(SCENARIO_DIMENSION_NS, metamodel.getScenarios().get(0).getDimensionNamespace());
		Assert.assertEquals(SCENARIO_DIMENSION_NS_PREFIX, metamodel.getScenarios().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SCENARIO_DIMENSION, metamodel.getScenarios().get(0).getDimensionName());
		Assert.assertEquals(SCENARIO_NAME_NS, metamodel.getScenarios().get(0).getValueNamespace());
		Assert.assertEquals(SCENARIO_NAME_NS_PREFIX, metamodel.getScenarios().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SCENARIO_NAME, metamodel.getScenarios().get(0).getValueName());
		Assert.assertFalse(metamodel.isInstant());
		Assert.assertFalse(metamodel.isForever());
	}

	@Test
	public void testContextMetamodelBuilderInstant() {
		XbrlContext metamodel = new XbrlContext.Builder(CONTEXT_ID, CONTEXT_SCHEMA, CONTEXT_NAME)
				.setInstant(PERIOD_END_DATE)
				.setSegment(
						new XbrlExplicitMember.Builder()
								.setDimension(SEGMENT_DIMENSION_NS, SEGMENT_DIMENSION_NS_PREFIX, SEGMENT_DIMENSION)
								.setValue(SEGMENT_NAME_NS, SEGMENT_NAME_NS_PREFIX, SEGMENT_NAME).build())
				.setScenario(
						new XbrlExplicitMember.Builder()
								.setDimension(SCENARIO_DIMENSION_NS, SCENARIO_DIMENSION_NS_PREFIX, SCENARIO_DIMENSION)
								.setValue(SCENARIO_NAME_NS, SCENARIO_NAME_NS_PREFIX, SCENARIO_NAME).build()).build();

		Assert.assertEquals(CONTEXT_ID, metamodel.getId());
		Assert.assertEquals(CONTEXT_SCHEMA, metamodel.getEntityScheme());
		Assert.assertEquals(CONTEXT_NAME, metamodel.getEntityName());
		Assert.assertNull(metamodel.getPeriodStartDate());
		Assert.assertEquals(PERIOD_END_DATE, metamodel.getPeriodEndDate());
		Assert.assertEquals(SEGMENT_DIMENSION_NS, metamodel.getSegments().get(0).getDimensionNamespace());
		Assert.assertEquals(SEGMENT_DIMENSION_NS_PREFIX, metamodel.getSegments().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SEGMENT_DIMENSION, metamodel.getSegments().get(0).getDimensionName());
		Assert.assertEquals(SEGMENT_NAME_NS, metamodel.getSegments().get(0).getValueNamespace());
		Assert.assertEquals(SEGMENT_NAME_NS_PREFIX, metamodel.getSegments().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SEGMENT_NAME, metamodel.getSegments().get(0).getValueName());
		Assert.assertEquals(SCENARIO_DIMENSION_NS, metamodel.getScenarios().get(0).getDimensionNamespace());
		Assert.assertEquals(SCENARIO_DIMENSION_NS_PREFIX, metamodel.getScenarios().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SCENARIO_DIMENSION, metamodel.getScenarios().get(0).getDimensionName());
		Assert.assertEquals(SCENARIO_NAME_NS, metamodel.getScenarios().get(0).getValueNamespace());
		Assert.assertEquals(SCENARIO_NAME_NS_PREFIX, metamodel.getScenarios().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SCENARIO_NAME, metamodel.getScenarios().get(0).getValueName());
		Assert.assertTrue(metamodel.isInstant());
		Assert.assertFalse(metamodel.isForever());
	}

	@Test
	public void testContextMetamodelBuilderForever() {
		XbrlContext metamodel = new XbrlContext.Builder(CONTEXT_ID, CONTEXT_SCHEMA, CONTEXT_NAME)
				.setSegment(
						new XbrlExplicitMember.Builder()
								.setDimension(SEGMENT_DIMENSION_NS, SEGMENT_DIMENSION_NS_PREFIX, SEGMENT_DIMENSION)
								.setValue(SEGMENT_NAME_NS, SEGMENT_NAME_NS_PREFIX, SEGMENT_NAME).build())
				.setScenario(
						new XbrlExplicitMember.Builder()
								.setDimension(SCENARIO_DIMENSION_NS, SCENARIO_DIMENSION_NS_PREFIX, SCENARIO_DIMENSION)
								.setValue(SCENARIO_NAME_NS, SCENARIO_NAME_NS_PREFIX, SCENARIO_NAME).build()).build();

		Assert.assertEquals(CONTEXT_ID, metamodel.getId());
		Assert.assertEquals(CONTEXT_SCHEMA, metamodel.getEntityScheme());
		Assert.assertEquals(CONTEXT_NAME, metamodel.getEntityName());
		Assert.assertNull(metamodel.getPeriodStartDate());
		Assert.assertNull(metamodel.getPeriodEndDate());
		Assert.assertEquals(SEGMENT_DIMENSION_NS, metamodel.getSegments().get(0).getDimensionNamespace());
		Assert.assertEquals(SEGMENT_DIMENSION_NS_PREFIX, metamodel.getSegments().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SEGMENT_DIMENSION, metamodel.getSegments().get(0).getDimensionName());
		Assert.assertEquals(SEGMENT_NAME_NS, metamodel.getSegments().get(0).getValueNamespace());
		Assert.assertEquals(SEGMENT_NAME_NS_PREFIX, metamodel.getSegments().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SEGMENT_NAME, metamodel.getSegments().get(0).getValueName());
		Assert.assertEquals(SCENARIO_DIMENSION_NS, metamodel.getScenarios().get(0).getDimensionNamespace());
		Assert.assertEquals(SCENARIO_DIMENSION_NS_PREFIX, metamodel.getScenarios().get(0).getDimensionNamespacePrefix());
		Assert.assertEquals(SCENARIO_DIMENSION, metamodel.getScenarios().get(0).getDimensionName());
		Assert.assertEquals(SCENARIO_NAME_NS, metamodel.getScenarios().get(0).getValueNamespace());
		Assert.assertEquals(SCENARIO_NAME_NS_PREFIX, metamodel.getScenarios().get(0).getValueNamespacePrefix());
		Assert.assertEquals(SCENARIO_NAME, metamodel.getScenarios().get(0).getValueName());
		Assert.assertFalse(metamodel.isInstant());
		Assert.assertTrue(metamodel.isForever());
	}

}
