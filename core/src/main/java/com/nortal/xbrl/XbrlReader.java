package com.nortal.xbrl;

import java.io.InputStream;

import com.nortal.xbrl.metamodel.XbrlInstance;

/**
 * Xbrl reader interface.
 */
public interface XbrlReader {

	/**
	 * Read xbrl instance xml stream in to instance meta model.
	 * 
	 * @param inputStream xbrl instance xml input stream
	 * @return instance meta model
	 */
	XbrlInstance read(InputStream inputStream);

}
