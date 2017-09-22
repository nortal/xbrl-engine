package com.nortal.xbrl.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.nortal.xbrl.XbrlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbrl._2003.instance.Context;
import org.xbrl._2003.instance.MonetaryItemType;
import org.xbrl._2003.instance.Unit;
import org.xbrl._2003.instance.Xbrl;
import org.xbrl._2006.xbrldi.ExplicitMember;
import org.xbrl.dtr.type.non_numeric.TextBlockItemType;
import org.xbrl.dtr.type.numeric.PerShareItemType;
import org.xbrl._2003.instance.Divide;
import org.xbrl._2003.instance.MeasuresType;

import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.metamodel.meta.XbrlExplicitMember;
import com.nortal.xbrl.metamodel.namespace.XbrlNamespaceMapping;

public class XbrlReaderImpl implements XbrlReader, Serializable {

	private static final long serialVersionUID = -556283761930040356L;
	private Logger LOG = LoggerFactory.getLogger(XbrlReaderImpl.class);

	private JAXBContext jaxbContext;

	public XbrlReaderImpl(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	@Override
	public XbrlInstance read(InputStream inputStream) {
		LOG.debug("Reading model from input stream");

		Xbrl xbrl = null;

		// Read File Stream
		StreamSource src = new StreamSource(inputStream);

		try {
			Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
			JAXBElement<Xbrl> doc = unmarshaller.unmarshal(src, Xbrl.class);
			// Read XBRL Elements
			xbrl = doc.getValue();
		}
		catch (JAXBException e) {
			throw new XbrlEngineException(e);
		}

		if (xbrl == null) {
			throw new XbrlEngineException("XBRL element is empty.");
		}

		return parseXbrl(xbrl);
	}

	protected XbrlInstance parseXbrl(Xbrl xbrl) {
		Map<String, String> unitIdMapping = new HashMap<String, String>();
		Map<String, String> contextIdMapping = new HashMap<String, String>();

		XbrlInstance instance = new XbrlInstance();
		instance.setMultiplier(XbrlValueEntry.Multiplier.THOUSAND);

		instance.setSchemaType(xbrl.getSchemaRef().get(0).getType());
		instance.setSchemaHref(xbrl.getSchemaRef().get(0).getHref());

		for (Object content : xbrl.getItemOrTupleOrContext()) {
			if (content instanceof Context) {
				parseXbrlContext(instance, (Context) content, contextIdMapping);
			}
			else if (content instanceof Unit) {
				parseXbrlUnit(instance, (Unit) content, unitIdMapping);
			}
		}

		for (Object content : xbrl.getItemOrTupleOrContext()) {
			if (!(content instanceof JAXBElement)) {
				continue;
			}

			parseXbrlElement(instance, (JAXBElement<?>) content, unitIdMapping, contextIdMapping);
		}

		return instance;
	}

	protected void parseXbrlContext(XbrlInstance instance, Context content, Map<String, String> idMapping) {
		try {
			XbrlContext.Builder metamodelBuilder = new XbrlContext.Builder().setEntitySchema(
					content.getEntity().getIdentifier().getScheme()).setEntityName(
					content.getEntity().getIdentifier().getValue());

			if (content.getPeriod().getInstant() != null) {
				metamodelBuilder.setInstant(content.getPeriod().getInstant());
			}
			else if (content.getPeriod().getForever() == null) {
				metamodelBuilder.setDateRange(content.getPeriod().getStartDate(), content.getPeriod().getEndDate());
			}

			if (content.getEntity().getSegment() != null) {
				for (Object explicitMemberObject : content.getEntity().getSegment().getAny()) {
					ExplicitMember explicitMember = (ExplicitMember) explicitMemberObject;

					metamodelBuilder.setSegment(new XbrlExplicitMember.Builder()
							.setDimension(explicitMember.getDimension().getNamespaceURI(),
									explicitMember.getDimension().getPrefix(),
									explicitMember.getDimension().getLocalPart())
							.setValue(explicitMember.getValue().getNamespaceURI(),
									explicitMember.getValue().getPrefix(), explicitMember.getValue().getLocalPart())
							.build());
				}
			}

			if (content.getScenario() != null) {
				for (Object explicitMemberObject : content.getScenario().getAny()) {
					ExplicitMember explicitMember = (ExplicitMember) explicitMemberObject;

					metamodelBuilder.setScenario(new XbrlExplicitMember.Builder()
							.setDimension(explicitMember.getDimension().getNamespaceURI(),
									explicitMember.getDimension().getPrefix(),
									explicitMember.getDimension().getLocalPart())
							.setValue(explicitMember.getValue().getNamespaceURI(),
									explicitMember.getValue().getPrefix(), explicitMember.getValue().getLocalPart())
							.build());
				}
			}

			XbrlContext model = metamodelBuilder.build();

			instance.addContext(model);
			idMapping.put(content.getId(), model.getId());
		}
		catch (ParseException e) {
			throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.content")
					.setArguments(content.getId()).build(), e);
		}
	}

	protected void parseXbrlUnit(XbrlInstance instance, Unit unit, Map<String, String> idMapping) {
		try {
			XbrlUnit model;
			if (unit.getMeasure() == null || unit.getMeasure().isEmpty()) {
				Divide divide = unit.getDivide();
				unit.setDivide(divide);
				MeasuresType unitDenominator = divide.getUnitDenominator();
				divide.setUnitDenominator(unitDenominator);
				MeasuresType unitNumerator = divide.getUnitNumerator();
				divide.setUnitNumerator(unitNumerator);

				model = new XbrlUnit.Builder()
						.setUnitNumeratorMeasure(unitNumerator.getMeasure().get(0).getLocalPart())
						.setUnitDenominatorMeasure(unitDenominator.getMeasure().get(0).getLocalPart()).build();
			} else {
				 model = new XbrlUnit.Builder().setMeasure(unit.getMeasure().get(0).getLocalPart()).build();
			}
			instance.addUnit(model);
			idMapping.put(unit.getId(), model.getId());
		}
		catch (Exception e) {
			throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.unit")
					.setArguments(unit.getId()).build(), e);
		}
	}

	protected void parseXbrlElement(XbrlInstance instance, JAXBElement<?> element, Map<String, String> unitIdMapping,
			Map<String, String> contextIdMapping) {
		XbrlValueEntry.Builder valueBuilder = new XbrlValueEntry.Builder();

		if (element.getValue() instanceof TextBlockItemType) {
			TextBlockItemType item = (TextBlockItemType) element.getValue();
			try {
				valueBuilder
						.setId(item.getId())
						.setNamespace(element.getName().getNamespaceURI())
						.setNamespacePrefix(
								XbrlNamespaceMapping.getNamespacePrefix(element.getName().getNamespaceURI()))
						.setName(element.getName().getLocalPart())
						.setContextId(contextIdMapping.get(((Context) item.getContextRef()).getId()))
						.setValue(item.getValue());
			}
			catch (Exception e) {
				throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.element")
						.setArguments(item.getId()).build(), e);
			}
		}
		else if (element.getValue() instanceof MonetaryItemType) {
			MonetaryItemType item = (MonetaryItemType) element.getValue();
			try {
				valueBuilder
						.setId(item.getId())
						.setNamespace(element.getName().getNamespaceURI())
						.setNamespacePrefix(
								XbrlNamespaceMapping.getNamespacePrefix(element.getName().getNamespaceURI()))
						.setName(element.getName().getLocalPart())
						.setContextId(contextIdMapping.get(((Context) item.getContextRef()).getId()))
						.setUnitId(unitIdMapping.get(((Unit) item.getUnitRef()).getId()))
						.setValue(item.getValue().toString());
				setMultiplier(instance, item.getDecimals(), item.getPrecision());
			}
			catch (Exception e) {
				throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.element")
						.setArguments(item.getId()).build(), e);
			}
		} else if (element.getValue() instanceof PerShareItemType) {
			PerShareItemType item = (PerShareItemType) element.getValue();
			try{
				valueBuilder
						.setId(item.getId())
						.setNamespace(element.getName().getNamespaceURI())
						.setNamespacePrefix(XbrlNamespaceMapping.getNamespacePrefix(element.getName().getNamespaceURI()))
						.setName(element.getName().getLocalPart())
						.setContextId(contextIdMapping.get(((Context) item.getContextRef()).getId()))
						.setUnitId(unitIdMapping.get(((Unit) item.getUnitRef()).getId()))
						.setValue(item.getValue().toString());
			}
			catch (Exception e) {
				throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.element")
						.setArguments(item.getId()).build(), e);
			}
		}
		else {
			throw new XbrlEngineException(new XbrlError.Builder().setCode("report.read.error.unsupported")
					.setArguments(element.getName()).build());
		}

		instance.addValue(valueBuilder.build());
	}

	private void setMultiplier(XbrlInstance instance, String decimals, String precision) {
		if ((decimals != null && Integer.parseInt(decimals) != -3)
				|| (precision != null && Integer.parseInt(precision) != 3)) {
			instance.setMultiplier(XbrlValueEntry.Multiplier.ONE);
		}
	}

	protected JAXBContext getJaxbContext() {
		return jaxbContext;
	}

}
