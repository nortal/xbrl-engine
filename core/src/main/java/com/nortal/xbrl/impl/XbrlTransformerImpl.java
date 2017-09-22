package com.nortal.xbrl.impl;

import com.nortal.xbrl.XbrlTransformer;
import com.nortal.xbrl.metamodel.TransformResult;
import com.nortal.xbrl.metamodel.XbrlContext;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlUnit;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.XbrlError;

import java.util.ArrayList;
import java.util.List;

public class XbrlTransformerImpl implements XbrlTransformer {

	@Override
	public TransformResult transform(XbrlInstance source, XbrlInstance target) {
		List<XbrlError> errors = new ArrayList<XbrlError>();

		// 1. Validate contexts
		for (XbrlContext contextModel : source.getContexts().values()) {
			if (target.getContext(contextModel.getId()) != null) {
				continue;
			}

			errors.add(new XbrlError.Builder().setCode("xbrl.transform.error.template.context")
					.setArguments(null, null, contextModel.getPeriodLabel()).build());
		}

		// 2. Validate units
		for (XbrlUnit unitModel : source.getUnits().values()) {
			if (target.getUnit(unitModel.getId()) != null) {
				continue;
			}

			errors.add(new XbrlError.Builder().setCode("xbrl.transform.error.template.measure")
					.setArguments(null, null, unitModel.getMeasure()).build());
		}

		// 3. Copy values from source to target, if target has appropriate context
		for (XbrlValueEntry valueEntryModel : source.getValues().values()) {
			// If we have found the value, then the context id is correct, no need to check it again
			XbrlValueEntry targetValueEntryModel = target.getValue(valueEntryModel.getId());

			if (targetValueEntryModel != null && valueEntryModel.getUnitId() == null) {
				targetValueEntryModel.setValue(valueEntryModel.getValue());
			}
			else if (targetValueEntryModel != null
					&& valueEntryModel.getUnitId().equals(targetValueEntryModel.getUnitId())) {
				targetValueEntryModel.setValue(valueEntryModel.getValue());
			}
			else if (targetValueEntryModel != null
					&& !valueEntryModel.getUnitId().equals(targetValueEntryModel.getUnitId())) {
				errors.add(new XbrlError.Builder().setCode("xbrl.transform.error.template.value.unit")
						.setArguments(null, null, valueEntryModel.getId()).build());
			}
			else {
				errors.add(new XbrlError.Builder().setCode("xbrl.transform.error.template.value")
						.setArguments(null, null, valueEntryModel.getId()).build());
			}
		}

		return new TransformResult(target, errors);
	}

}
