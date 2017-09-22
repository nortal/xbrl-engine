package com.nortal.xbrl.view.entity;

import com.nortal.xbrl.metamodel.meta.XbrlMetamodel;
import lombok.Data;

import java.util.Date;

@Data
public class ReportField {

    private static final long serialVersionUID = 3732464990015572635L;

    private Long id;
    private String type;
    private String name;
    private Object value;
    private Report report;
    private Date periodStartDate;
    private Date periodEndDate;

    public String getKey() {
        String key = name;
        if (periodStartDate != null) {
            key += "_s" + XbrlMetamodel.DATE_FORMAT.get().format(periodStartDate);
        }
        if (periodEndDate != null) {
            key += "_e" + XbrlMetamodel.DATE_FORMAT.get().format(periodEndDate);
        }
        return key;
    }

    public String getValueString() {
        // TODO depending on type get correct string representation
        return value == null ? null : getValue().toString();
    }

}
