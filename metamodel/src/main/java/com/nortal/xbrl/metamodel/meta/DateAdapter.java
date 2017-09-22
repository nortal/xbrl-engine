package com.nortal.xbrl.metamodel.meta;

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateAdapter extends XmlAdapter<String, Date> {

	@Override
	public String marshal(Date date) throws Exception {
		return XbrlMetamodel.DATE_FORMAT.get().format(date);
	}

	@Override
	public Date unmarshal(String date) throws Exception {
		return XbrlMetamodel.DATE_FORMAT.get().parse(date);
	}
}