@XmlSchema(elementFormDefault = XmlNsForm.QUALIFIED, xmlns = {
	@XmlNs(namespaceURI = "http://www.w3.org/2001/XMLSchema", prefix = "xsd"),
	@XmlNs(namespaceURI = "http://www.w3.org/1999/xlink", prefix = "xlink"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2003/instance", prefix = "xbrli"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2003/linkbase", prefix = "link"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2003/XLink", prefix = "xl"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/dtr/type/numeric", prefix = "num"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/dtr/type/non-numeric", prefix = "nonnum"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2003/iso4217", prefix = "iso4217"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2009/role/reference", prefix = "reference"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2009/role/net", prefix = "net"),
	@XmlNs(namespaceURI = "http://www.xbrl.org/2009/role/negated", prefix = "negated"),
	@XmlNs(namespaceURI = "http://xbrl.org/2006/xbrldi", prefix = "xbrldi"),
	@XmlNs(namespaceURI = "http://xbrl.org/2008/generic", prefix = "gen"),
	@XmlNs(namespaceURI = "http://xbrl.org/2005/xbrldt", prefix = "xbrldt"),
	@XmlNs(namespaceURI = "http://xbrl.org/2008/label", prefix = "label"),
	@XmlNs(namespaceURI = "http://xbrl.iasb.org/info", prefix = "info"),
	@XmlNs(namespaceURI = "http://xbrl.ifrs.org/taxonomy/2017-03-09/ifrs-full", prefix = "ifrs-full"),
	@XmlNs(namespaceURI = "http://xbrl.ifrs.org/taxonomy/2017-03-09/ifrs-mc", prefix = "ifrs-mc")
})
package com.nortal.xbrl.metamodel.namespace;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;

