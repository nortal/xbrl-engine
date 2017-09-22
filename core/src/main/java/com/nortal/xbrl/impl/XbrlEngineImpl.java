package com.nortal.xbrl.impl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import com.nortal.xbrl.XbrlReader;
import org.xbrl._2003.instance.Xbrl;

import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.XbrlBuilder;
import com.nortal.xbrl.XbrlCalculator;
import com.nortal.xbrl.XbrlEngine;
import com.nortal.xbrl.XbrlMapper;
import com.nortal.xbrl.XbrlTransformer;
import com.nortal.xbrl.XbrlValidator;
import com.nortal.xbrl.metamodel.namespace.XbrlNamespaceMapping;

public class XbrlEngineImpl implements XbrlEngine {

	/**
	 * Types for the JAXBContext. Need a type from each package for the elements / types to be loaded into the context.
	 */
	private static final Class<?>[] JAXB_CONTEXT_TYPES = new Class[] {
			Xbrl.class,
			XbrlNamespaceMapping.class,
			org.xbrl._2003.linkbase.Linkbase.class,
			org.xbrl._2005.xbrldt.ObjectFactory.class,
			org.xbrl._2006.xbrldi.ObjectFactory.class,
			org.ifrs.xbrl.taxonomy._2016_03_31.ifrs.ObjectFactory.class,
			org.ifrs.xbrl.taxonomy._2016_03_31.ifrs_full.ObjectFactory.class,
			org.ifrs.xbrl.taxonomy._2017_03_09.ifrs_mc.ObjectFactory.class,
			org.ifrs.xbrl.taxonomy._2017_03_09.ifrs_smes.ObjectFactory.class,
			org.ifrs.xbrl.taxonomy._2017_03_09.ifrs_full.ObjectFactory.class
	};

	private JAXBContext jaxbContext;

	public JAXBContext getJaxbContext() {
		if (jaxbContext != null) {
			return jaxbContext;
		}

		try {
			return jaxbContext = JAXBContext.newInstance(JAXB_CONTEXT_TYPES);
		}
		catch (JAXBException e) {
			throw new XbrlEngineException(e);
		}
	}

	@Override
	public XbrlReader getReader() {
		return new XbrlReaderImpl(getJaxbContext());
	}

	@Override
	public XbrlMapper getMapper() {
		return new XbrlMapperImpl(getJaxbContext(), JAXB_CONTEXT_TYPES);
	}

	@Override
	public XbrlWriterImpl getWriter() {
		return new XbrlWriterImpl(getJaxbContext(), getMapper());
	}

	@Override
	public XbrlValidator getValidator() {
		return new XbrlValidatorImpl();
	}

	@Override
	public XbrlCalculator getCalculator() {
		return new XbrlCalculatorImpl();
	}

	@Override
	public XbrlBuilder getBuilder() {
		return new XbrlBuilderImpl();
	}

	@Override
	public XbrlTransformer getTransformer() {
		return new XbrlTransformerImpl();
	}

}
