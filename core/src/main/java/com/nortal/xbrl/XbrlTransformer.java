package com.nortal.xbrl;


import com.nortal.xbrl.metamodel.TransformResult;
import com.nortal.xbrl.metamodel.XbrlInstance;

public interface XbrlTransformer {

	/**
	 * Transform data from source in to target instance model. Any transformation errors will be returned inside transformation result.
	 * 
	 * @param source source instance
	 * @param target target instance
	 * @return transformation result
	 */
	TransformResult transform(XbrlInstance source, XbrlInstance target);

}
