package com.nortal.xbrl;

/**
 * Xbrl engine interface.
 */

public interface XbrlEngine {

	/**
	 * Get reader instance.
	 * 
	 * @return reader instance
	 */
	XbrlReader getReader();

	/**
	 * Get mapper instance.
	 * 
	 * @return mapper instance
	 */
	XbrlMapper getMapper();

	/**
	 * Get writer instance.
	 * 
	 * @return writer instance
	 */
	XbrlWriter getWriter();

	/**
	 * Get validator instance.
	 * 
	 * @return validator instance
	 */
	XbrlValidator getValidator();

	/**
	 * Get calculator instance.
	 * 
	 * @return calculator instance
	 */
	XbrlCalculator getCalculator();

	/**
	 * Get builder instance.
	 * 
	 * @return builder instance
	 */
	XbrlBuilder getBuilder();

	/**
	 * Get transformer instance.
	 * 
	 * @return transformer instance
	 */
	XbrlTransformer getTransformer();

}
