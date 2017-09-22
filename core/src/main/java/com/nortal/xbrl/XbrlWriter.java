package com.nortal.xbrl;

import java.io.OutputStream;

import com.nortal.xbrl.metamodel.XbrlInstance;

/**
 * Xbrl writer interface.
 */
public interface XbrlWriter {

	/**
	 * Write instance metamodel to xbrl instance xml.
	 * 
	 * @param model the instance model
	 * @param outputStream the output stream to write to
	 */
	void write(XbrlInstance model, OutputStream outputStream);

	byte[] write(XbrlInstance xbrlInstance);

}
