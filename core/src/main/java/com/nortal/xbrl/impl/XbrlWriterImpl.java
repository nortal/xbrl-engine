package com.nortal.xbrl.impl;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.nortal.xbrl.XbrlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbrl._2003.instance.Context;
import org.xbrl._2003.instance.ContextEntityType;
import org.xbrl._2003.instance.ContextPeriodType;
import org.xbrl._2003.instance.ContextScenarioType;
import org.xbrl._2003.instance.MonetaryItemType;
import org.xbrl._2003.instance.Segment;
import org.xbrl._2003.instance.Unit;
import org.xbrl._2003.instance.Divide;
import org.xbrl._2003.instance.MeasuresType;
import org.xbrl._2003.instance.Xbrl;
import org.xbrl._2003.iso4217.Currency;
import org.xbrl._2003.xlink.SimpleType;
import org.xbrl._2006.xbrldi.ExplicitMember;
import org.xbrl.dtr.type.non_numeric.TextBlockItemType;
import org.xbrl.dtr.type.numeric.PerShareItemType;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.XbrlMapper;

public class XbrlWriterImpl implements XbrlWriter {

	private Logger LOG = LoggerFactory.getLogger(XbrlWriterImpl.class);

	private JAXBContext jaxbContext;

	private XbrlMapper mapper;

	public XbrlWriterImpl(JAXBContext jaxbContext, XbrlMapper mapper) {
		this.jaxbContext = jaxbContext;
		this.mapper = mapper;
	}

	@Override
	public void write(XbrlInstance model, OutputStream outputStream) {
		LOG.debug("Writing instance {}", model);

		// Temporal storage for fast context and unit search by ID
		Map<String, Object> contextList = new HashMap<String, Object>();
		Map<String, Object> unitList = new HashMap<String, Object>();

		// Create new XBRL instance
		Xbrl xbrl = new Xbrl();

		// 1. Set schema ref
		SimpleType schemaRef = new SimpleType();
		schemaRef.setType(model.getSchemaType());
		schemaRef.setHref(model.getSchemaHref());
		xbrl.getSchemaRef().add(schemaRef);

		// 2. Set context`s
		for (XbrlContext metamodel : model.getContexts().values()) {
			// 2.1 Create context
			Context context = new Context();
			context.setId(metamodel.getId());

			// 2.2 Set entity
			ContextEntityType contextEntityType = new ContextEntityType();
			ContextEntityType.Identifier identifier = new ContextEntityType.Identifier();
			identifier.setScheme(metamodel.getEntityScheme());
			identifier.setValue(metamodel.getEntityName());
			contextEntityType.setIdentifier(identifier);
			context.setEntity(contextEntityType);

			// 2.3 Set period
			ContextPeriodType contextPeriodType = new ContextPeriodType();
			if (metamodel.isForever()) {
				contextPeriodType.setForever(new ContextPeriodType.Forever());
			}
			else if (metamodel.isInstant()) {
				contextPeriodType.setInstant(XbrlMetamodel.DATE_FORMAT.get().format(metamodel.getPeriodEndDate()));
			}
			else {
				contextPeriodType.setStartDate(XbrlMetamodel.DATE_FORMAT.get().format(metamodel.getPeriodStartDate()));
				contextPeriodType.setEndDate(XbrlMetamodel.DATE_FORMAT.get().format(metamodel.getPeriodEndDate()));
			}
			context.setPeriod(contextPeriodType);

			// 2.4 Add entity segments
			if (!metamodel.getSegments().isEmpty()) {
				Segment segment = new Segment();

				for (XbrlExplicitMember xbrlExplicitMember : metamodel.getSegments()) {
					ExplicitMember explicitMember = new ExplicitMember();
					explicitMember.setDimension(new QName(xbrlExplicitMember.getDimensionNamespace(),
							xbrlExplicitMember.getDimensionName()));
					explicitMember.setValue(new QName(xbrlExplicitMember.getValueNamespace(),
							xbrlExplicitMember.getValueName()));
					segment.getAny().add(explicitMember);
				}

				context.getEntity().setSegment(segment);
			}

			// 2.5 Add scenarios
			if (!metamodel.getScenarios().isEmpty()) {
				ContextScenarioType contextScenarioType = new ContextScenarioType();

				for (XbrlExplicitMember xbrlExplicitMember : metamodel.getScenarios()) {
					ExplicitMember explicitMember = new ExplicitMember();
					explicitMember.setDimension(new QName(xbrlExplicitMember.getDimensionNamespace(),
							xbrlExplicitMember.getDimensionName()));
					explicitMember.setValue(new QName(xbrlExplicitMember.getValueNamespace(),
							xbrlExplicitMember.getValueName()));
					contextScenarioType.getAny().add(explicitMember);
				}

				context.setScenario(contextScenarioType);
			}

			// Context created, add it to XBRL
			contextList.put(context.getId(), context);
			xbrl.getItemOrTupleOrContext().add(context);
		}

		// 3. Set unit`s
		for (XbrlUnit metamodel : model.getUnits().values()) {
			// 3.1 Create Unit
			Unit unit = new Unit();
			unit.setId(metamodel.getId());
			if (metamodel.getMeasure() == null){
				Divide divide = new Divide();
				unit.setDivide(divide);
				MeasuresType unitNumerator = new MeasuresType();
				divide.setUnitNumerator(unitNumerator);
				unitNumerator.getMeasure().add(	new QName("http://www.xbrl.org/2003/iso4217", Currency.valueOf(metamodel.getUnitNumeratorMeasure()).value(),
						"iso4217"));

				MeasuresType unitDenominator = new MeasuresType();
				divide.setUnitDenominator(unitDenominator);
				unitDenominator.getMeasure().add( new QName( "http://www.xbrl.org/2003/instance", metamodel.getUnitDenominatorMeasure(), "xbrli"));
			} else {
				unit.getMeasure().add(
						new QName("http://www.xbrl.org/2003/iso4217", Currency.valueOf(metamodel.getMeasure()).value(),
								"iso4217"));
			}

			// Unit created, add it to XBRL
			unitList.put(unit.getId(), unit);
			xbrl.getItemOrTupleOrContext().add(unit);
		}

		// 4. Set value`s
		for (XbrlValueEntry metamodel : model.getValues().values()) {
			// not writing null values
			if (metamodel.isEmpty()) {
				continue;
			}

			// 4.1 Create element that stores the value
			JAXBElement<Object> element = createJAXBElement(metamodel.getNamespace(), metamodel.getName());

			// 4.2 Depending on the declared type create the actual item type and set the value
			if (element.getDeclaredType().isAssignableFrom(TextBlockItemType.class)) {
				TextBlockItemType textBlockItemType = new TextBlockItemType();
				textBlockItemType.setId(metamodel.getId());
				textBlockItemType.setContextRef(contextList.get(metamodel.getContextId()));
				textBlockItemType.setValue(metamodel.getValue());

				element.setValue(textBlockItemType);
			}
			else if (element.getDeclaredType().isAssignableFrom(MonetaryItemType.class)) {
				MonetaryItemType monetaryItemType = new MonetaryItemType();
				monetaryItemType.setId(metamodel.getId());
				monetaryItemType.setDecimals(String.valueOf(model.getMultiplier().getDecimals()));
				monetaryItemType.setUnitRef(unitList.get(metamodel.getUnitId()));
				monetaryItemType.setContextRef(contextList.get(metamodel.getContextId()));
				monetaryItemType.setValue(metamodel.getValueAsBigDecimal());

				element.setValue(monetaryItemType);
			}
			else if (element.getDeclaredType().isAssignableFrom(PerShareItemType.class)) {
				PerShareItemType perShareItemType = new PerShareItemType();
				perShareItemType.setId(metamodel.getId());
				perShareItemType.setContextRef(contextList.get(metamodel.getContextId()));
				perShareItemType.setUnitRef(unitList.get(metamodel.getUnitId()));
				perShareItemType.setValue(metamodel.getValueAsBigDecimal());

				element.setValue(perShareItemType);
			}
			else {
				throw new XbrlEngineException("Unsupported declared type: " + element.getDeclaredType());
			}

			// Element created, add it to XBRL
			xbrl.getItemOrTupleOrContext().add(element);
		}

		LOG.debug("XBRL model {}", xbrl);

		try {
			Marshaller marshaller = getJaxbContext().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			marshaller.marshal(xbrl, outputStream);
		}
		catch (JAXBException e) {
			throw new XbrlEngineException(e);
		}
	}

	@Override
	public byte[] write(XbrlInstance xbrlInstance) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		write(xbrlInstance, bos);
		return bos.toByteArray();
	}

	@SuppressWarnings("unchecked")
	protected JAXBElement<Object> createJAXBElement(String namespace, String name) {
		return (JAXBElement<Object>) getXbrlMapper().createElement(namespace, name);
	}

	protected JAXBContext getJaxbContext() {
		return jaxbContext;
	}

	protected XbrlMapper getXbrlMapper() {
		return mapper;
	}

}
