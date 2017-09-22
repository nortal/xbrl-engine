package com.nortal.xbrl.impl;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.namespace.QName;

import com.nortal.xbrl.XbrlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nortal.xbrl.metamodel.meta.AbstractReportEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import com.nortal.xbrl.model.XbrlElement;
import com.nortal.xbrl.metamodel.namespace.XbrlNamespaceMapping;
import com.nortal.xbrl.metamodel.util.MetamodelUnmarshaller;
import com.nortal.xbrl.metamodel.util.MetamodelUtil;

public class XbrlMapperImpl implements XbrlMapper {

	private static final Logger LOG = LoggerFactory.getLogger(XbrlMapperImpl.class);

	private JAXBContext jaxbContext;

	private Class<?>[] jaxbContextTypes;

	private Map<QName, XbrlElement> elements;

	public XbrlMapperImpl(JAXBContext jaxbContext, Class<?>[] jaxbContextTypes) {
		this.jaxbContext = jaxbContext;
		this.jaxbContextTypes = jaxbContextTypes;
	}

	@Override
	public Map<String, ReportingFormMetamodel> getReportMetamodels() {
		return getReportingFormMetamodels(getResourceByName("metamodel.xml"));
	}

	@Override
	public Map<String, ReportingFormMetamodel> getReportingFormMetamodels(InputStream inputStream) {
		LOG.debug("Getting all mappings");
		Map<String, ReportingFormMetamodel> models = new HashMap<String, ReportingFormMetamodel>();

		XbrlMetamodel model;

		try {
			model = MetamodelUtil.deserialize(inputStream, new MetamodelUnmarshaller() {
				@Override
				public void reportEntryMetamodelUnmarshal(AbstractReportEntry reportEntryMetamodel) {
					reportEntryMetamodel.setNamespacePrefix(XbrlNamespaceMapping
							.getNamespacePrefix(reportEntryMetamodel.getNamespace()));
				}
			});
		}
		catch (JAXBException e) {
			throw new XbrlEngineException(e);
		}

		Collections.sort(model.getReports());

		for (ReportingFormMetamodel reportingForm : model.getReports()) {
			models.put(reportingForm.getCode(), reportingForm);
		}

		return models;
	}

	@Override
	public Object createElement(String namespace, String name) {
		XbrlElement element = findElementByQName(new QName(namespace, name));

		if (element == null) {
			throw new XbrlEngineException("Unable to create element, namespace: " + namespace + ", local name: " + name);
		}

		try {
			if (element.isJAXBElement()) {
				return createJAXBElement(element.getQName(), element.getActualReturnType());
			}

			return element.getReturnType().getClass().getConstructor().newInstance();
		}
		catch (Exception e) {
			throw new XbrlEngineException(e);
		}
	}

	protected <T> JAXBElement<T> createJAXBElement(QName qname, Class<T> type) {
		return new JAXBElement<T>(qname, type, null, null);
	}

	protected XbrlElement findElementByQName(QName elementQName) {
		return getElements().get(elementQName);
	}

	protected Map<QName, XbrlElement> getElements() {
		if (elements != null) {
			return elements;
		}

		return elements = getAllElementsFromContextTypes();
	}

	protected Map<QName, XbrlElement> getAllElementsFromContextTypes() {
		JAXBIntrospector ji = getJaxbContext().createJAXBIntrospector();
		Map<QName, XbrlElement> elements = new HashMap<QName, XbrlElement>();

		for (Class<?> clazz : jaxbContextTypes) {
			QName qName = null;
			try {
				qName = ji.getElementName(clazz.newInstance());
			}
			catch (Exception e) {
				// re-throw the exception
				throw new XbrlEngineException(e);
			}

			// Did we read a class with XmlRootElement annotation?
			if (null != qName) {
				XbrlElement element = new XbrlElement();
				element.setNamespace(qName.getNamespaceURI());
				element.setName(qName.getLocalPart());
				element.setReturnType(clazz);

				elements.put(element.getQName(), element);
			}
			else {
				// We must have read a class with XmlRegistry annotation, parse individual elements inside.
				elements.putAll(getAllElementsFromClass(clazz));
			}
		}

		return elements;
	}

	protected Map<QName, XbrlElement> getAllElementsFromClass(Class<?> clazz) {
		HashMap<QName, XbrlElement> elements = new HashMap<QName, XbrlElement>();

		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			try {
				// Check if method has XmlElementDecl annotation
				XmlElementDecl annotation = method.getAnnotation(XmlElementDecl.class);

				if (annotation == null) {
					continue;
				}

				XbrlElement mapping = new XbrlElement();
				mapping.setNamespace(annotation.namespace());
				mapping.setName(annotation.name());

				if (method.getGenericReturnType() instanceof ParameterizedType) {
					ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
					mapping.setReturnType((Class<?>) parameterizedType.getRawType());
					mapping.setActualReturnType((Class<?>) parameterizedType.getActualTypeArguments()[0]);
				}
				else {
					throw new XbrlEngineException("The method does not return a parameterized return type.");
				}

				elements.put(mapping.getQName(), mapping);
			}
			catch (Exception e) {
				// re-throw the exception
				throw new XbrlEngineException(e);
			}
		}

		return elements;
	}

	protected JAXBContext getJaxbContext() {
		return jaxbContext;
	}

	protected InputStream getResourceByName(String name) {
		return this.getClass().getClassLoader().getResourceAsStream(name);
	}

}
