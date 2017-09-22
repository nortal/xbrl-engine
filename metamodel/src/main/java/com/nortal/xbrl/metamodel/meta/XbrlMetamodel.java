package com.nortal.xbrl.metamodel.meta;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "xbrl")
public class XbrlMetamodel {

	public static final String PARTS_DELIMITER = "_";
	public static final String PREFIX_DELIMITER = ":";
	public static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
		@Override
		public DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd");
		}
	};

	private List<ReportingFormMetamodel> reports;
	private Date taxonomyVersionDate;

	public XbrlMetamodel() {
		reports = new ArrayList<ReportingFormMetamodel>();
	}

	@XmlElementWrapper(name = "forms")
	@XmlElement(name = "entry")
	public List<ReportingFormMetamodel> getReports() {
		return reports;
	}

	public void setReports(List<ReportingFormMetamodel> reports) {
		this.reports = reports;
	}

	@XmlJavaTypeAdapter(DateAdapter.class)
	@XmlAttribute(name = "taxonomyVersionDate")
	public Date getTaxonomyVersionDate() {
		return taxonomyVersionDate;
	}

	public void setTaxonomyVersionDate(Date taxonomyVersionDate) {
		this.taxonomyVersionDate = taxonomyVersionDate;
	}
}
