package com.nortal.xbrl.model;

import java.io.Serializable;
import java.util.Date;

public class ReportingFormType implements Serializable, Comparable<ReportingFormType> {
    private static final long serialVersionUID = -980412095410687504L;

    private Long id;
    private String code;
    private Date taxonomyVersionDate;
    private String name;
    private StatementType statementType;
    private boolean containsData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getTaxonomyVersionDate() {
        return taxonomyVersionDate;
    }

    public void setTaxonomyVersionDate(Date taxonomyVersionDate) {
        this.taxonomyVersionDate = taxonomyVersionDate;
    }

    public StatementType getStatementType() {
        return statementType;
    }

    public void setStatementType(StatementType statementType) {
        this.statementType = statementType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean containsData() {
        return containsData;
    }

    public void setContainsData(boolean containsData) {
        this.containsData = containsData;
    }

    @Override
    public int compareTo(ReportingFormType o) {
        return code.compareTo(o.getCode());
    }
}
