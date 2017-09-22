package com.nortal.xbrl.model;

import java.io.Serializable;

public class StatementType implements Serializable, Comparable<StatementType> {
    private static final long serialVersionUID = -5890051575098108432L;

    private Long id;
    private String code;
    private String name;
    private Boolean mandatory;
    private int comparativePeriod;

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

    public Boolean getMandatory() {
        return mandatory;
    }

    public void setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getComparativePeriod() {
        return comparativePeriod;
    }

    public void setComparativePeriod(int comparativePeriod) {
        this.comparativePeriod = comparativePeriod;
    }

    public boolean hasComparativePeriod(){
       return comparativePeriod > 0;
    }

    @Override
    public int compareTo(StatementType o) {
        return id.compareTo(o.getId());
    }
}
