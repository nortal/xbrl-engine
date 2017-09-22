package com.nortal.xbrl.metamodel.util;

import javax.xml.bind.Unmarshaller;

import com.nortal.xbrl.metamodel.meta.AbstractReportEntry;

public abstract class MetamodelUnmarshaller extends Unmarshaller.Listener {

	@Override
	public void beforeUnmarshal(Object target, Object parent) {
		super.beforeUnmarshal(target, parent);
	}

	@Override
	public void afterUnmarshal(Object target, Object parent) {
		super.afterUnmarshal(target, parent);
		if (target instanceof AbstractReportEntry) {
			reportEntryMetamodelUnmarshal((AbstractReportEntry) target);
		}
	}

	public abstract void reportEntryMetamodelUnmarshal(AbstractReportEntry reportEntryMetamodel);

}
