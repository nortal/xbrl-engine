package com.nortal.xbrl.metamodel.namespace;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;

/**
 * This class is needed for the JAXBContext, to define the correct namespace prefix. It also provides helper method to
 * get namespace prefix by namespaceURI
 */
public final class XbrlNamespaceMapping {

	public static String getNamespacePrefix(String namespaceURI) {
		XmlSchema xmlSchema = XbrlNamespaceMapping.class.getPackage().getAnnotation(XmlSchema.class);
		for (XmlNs xmlNs : xmlSchema.xmlns()) {
			if (xmlNs.namespaceURI().equals(namespaceURI)) {
				return xmlNs.prefix();
			}
		}
		return null;
	}

	public static String getNamespaceURI(String namespacePrefix) {
		XmlSchema xmlSchema = XbrlNamespaceMapping.class.getPackage().getAnnotation(XmlSchema.class);
		for (XmlNs xmlNs : xmlSchema.xmlns()) {
			if (xmlNs.prefix().equals(namespacePrefix)) {
				return xmlNs.namespaceURI();
			}
		}
		return null;
	}
}
