package com.nortal.xbrl.view.model;

import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.view.entity.Report;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
public class ReportForm implements Serializable {

    private static final long serialVersionUID = -1115042336585557052L;

    public static final String MODEL_ATTRIBUTE_NAME = "reportForm";

    private Report report;
    private XbrlInstance xbrlInstance;
    private List<ReportingFormMetamodel> reportingFormMetamodels;
    private Map<String, Object> values;

    public String getReportingFormName(String code, String lang) {
        for (ReportingFormMetamodel metamodel : reportingFormMetamodels) {
            if (metamodel.getCode().equals(code)) {
                return metamodel.getName().get(lang);
            }
        }
        return null;
    }

    public List<ReportingFormMetamodel> getReportingFormMetamodels() {
        return reportingFormMetamodels;
    }

    public boolean hasValues() {
        return values != null;
    }

    public Map<String, Object> getValues() {
        if (values == null && xbrlInstance != null) {
            return xbrlInstance.getDisplayValues();
        }

        return values;
    }
}
