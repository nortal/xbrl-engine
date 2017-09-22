package com.nortal.xbrl.metamodel.meta;


/**
 * Generic xbrl engine exception. Should be used instead of any other exception in the engine.
 */
public class XbrlEngineException extends RuntimeException {

	private static final long serialVersionUID = 780803056610478644L;
	private XbrlError error;

	public XbrlEngineException(String message) {
		super(message);
	}

	public XbrlEngineException(String message, Throwable cause) {
		super(message, cause);
	}

	public XbrlEngineException(XbrlError error) {
		super(error.getCode());
		this.error = error;
	}

	public XbrlEngineException(XbrlError error, Throwable cause) {
		super(cause);
		this.error = error;
	}

	public XbrlEngineException(Throwable cause) {
		super(cause);
	}

	public XbrlError getError() {
		return error;
	}

}
