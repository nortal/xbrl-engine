package com.nortal.xblr.metamodel.generator;

import java.util.List;

import org.xbrlapi.Arc;
import org.xbrlapi.ArcEnd;
import org.xbrlapi.Locator;
import org.xbrlapi.utilities.XBRLException;

public class MetamodelBuilderUtil {

	public static Locator getTargetLocator(Arc arc) throws XBRLException {
		List<ArcEnd> targets = arc.getTargetFragments();
		if (targets.size() != 1) {
			throw new IllegalStateException("Expecting 1 target fragment, but found " + targets.size());
		}
		return (Locator) targets.iterator().next();
	}

}
