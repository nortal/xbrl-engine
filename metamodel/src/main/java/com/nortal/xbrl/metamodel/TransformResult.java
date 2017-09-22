package com.nortal.xbrl.metamodel;

import com.nortal.xbrl.metamodel.meta.XbrlError;

import java.io.Serializable;
import java.util.List;

/**
 * @author Priit Üksküla
 */
public class TransformResult implements Serializable {
    private static final long serialVersionUID = 7448148378796451349L;

    private XbrlInstance xbrlInstance;
    private List<XbrlError> xbrlErrors;

    public TransformResult(XbrlInstance xbrlInstance, List<XbrlError> xbrlErrors) {
        this.xbrlInstance = xbrlInstance;
        this.xbrlErrors = xbrlErrors;
    }

    public XbrlInstance getXbrlInstance() {
        return xbrlInstance;
    }

    public void setXbrlInstance(XbrlInstance xbrlInstance) {
        this.xbrlInstance = xbrlInstance;
    }

    public List<XbrlError> getXbrlErrors() {
        return xbrlErrors;
    }

    public void setXbrlErrors(List<XbrlError> xbrlErrors) {
        this.xbrlErrors = xbrlErrors;
    }
}
