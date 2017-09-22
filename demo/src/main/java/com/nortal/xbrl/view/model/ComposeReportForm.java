package com.nortal.xbrl.view.model;

import com.nortal.xbrl.util.PeriodUtil;
import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.model.StatementType;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.view.entity.Report;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class ComposeReportForm implements Serializable {

    private static final long serialVersionUID = 444406448732426206L;

    public static final String MODEL_ATTRIBUTE_NAME = "composeReportForm";

    private Report report;
    private List<String> selectedFormCodes;
    private transient MultipartFile instanceFile;
    private XbrlInstance xbrlInstance;
    private List<ReportingFormType> availableForms;
    private List<ReportingFormMetamodel> reportingFormMetamodels;

    public List<StatementType> getStatementTypes() {
        List<StatementType> statementTypes = new ArrayList<StatementType>();
        for (ReportingFormType availableForm : availableForms) {
            if (!statementTypes.contains(availableForm.getStatementType())) {
                statementTypes.add(availableForm.getStatementType());
            }
        }
        Collections.sort(statementTypes);
        return statementTypes;
    }

    public List<ReportingFormType> getAvailableForms(StatementType statementType) {
        List<ReportingFormType> result = new ArrayList<ReportingFormType>();
        for (ReportingFormType availableForm : availableForms) {
            if (availableForm.getStatementType() == statementType) {
                result.add(availableForm);
            }
        }
        return result;
    }

    public boolean getIsFormSelected(ReportingFormType formType) {
        for (ReportingFormType type : report.getSelectedForms()) {
            if (formType.getCode().equals(type.getCode())) return true;
        }

        return false;
    }

    public List<ReportingFormType> bindSelectedForms() {
        List<ReportingFormType> selectedForms = new ArrayList<ReportingFormType>();
        for (ReportingFormType availableForm : availableForms) {
            if (selectedFormCodes.contains(availableForm.getCode())) {
                selectedForms.add(availableForm);
            }
        }
        Collections.sort(selectedForms);
        return selectedForms;
    }

    public void createEmptySelectedCodes() {
        selectedFormCodes = new ArrayList<String>();
        for (int i = 0; i < availableForms.size(); i++) {
            selectedFormCodes.add(null);
        }
    }

    public String getReportingFormName(String code, String lang) {
        for (ReportingFormMetamodel metamodel : reportingFormMetamodels) {
            if (metamodel.getCode().equals(code)) {
                return metamodel.getName().get(lang);
            }
        }
        return null;
    }
    
    public Integer getAccountingYear() {
        return PeriodUtil.getAccountingYear(report.getPeriodEndDate());
    }

}
