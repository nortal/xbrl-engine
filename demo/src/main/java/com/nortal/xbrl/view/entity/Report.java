package com.nortal.xbrl.view.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import lombok.Data;

@Data
public class Report implements Serializable {
    private static final long serialVersionUID = -7593636346901428456L;

    private Long id;
    private Date periodStartDate;
    private Date periodEndDate;
    private Date precedingPeriodStartDate;
    private Date precedingPeriodEndDate;
    private int accountingYear;
    private XbrlValueEntry.Multiplier multiplier;
    private List<ReportingFormType> selectedForms;

}
