package com.nortal.xbrl.view.controller;

import com.nortal.xbrl.model.ReportingFormType;
import com.nortal.xbrl.model.StatementType;
import com.nortal.xbrl.metamodel.TransformResult;
import com.nortal.xbrl.metamodel.XbrlInstance;
import com.nortal.xbrl.metamodel.XbrlValueEntry;
import com.nortal.xbrl.metamodel.meta.ReportingFormMetamodel;
import com.nortal.xbrl.metamodel.meta.XbrlEngineException;
import com.nortal.xbrl.metamodel.meta.XbrlError;
import com.nortal.xbrl.view.entity.Report;
import com.nortal.xbrl.view.model.ComposeReportForm;
import com.nortal.xbrl.view.model.ReportForm;
import com.nortal.xbrl.view.service.ReportService;
import com.nortal.xbrl.view.service.XbrlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Slf4j
@Controller
public class DemoController {

    private static final String CONTEXTS = "contexts";
    private static final String CURRENT_FORM = "currentForm";
    private static final String FORM_METAMODEL = "formMetamodel";

    @Autowired
    private XbrlService xbrlService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private MessageSource messageSource;

    @RequestMapping(value = "/")
    public String index() {
        return "index";
    }

    @RequestMapping(value = "/start")
    public String newReport() {
        Report report = reportService.createReport();

        return "redirect:/" + report.getId();
    }

    @RequestMapping(value = "/{reportId:[\\d]+}")
    public String selectForms(@PathVariable Long reportId,
                                Map<String, Object> model) {
        if (!model.containsKey(ComposeReportForm.MODEL_ATTRIBUTE_NAME)) {
            ComposeReportForm form = setupComposeReportForm(reportId);
            model.put(ComposeReportForm.MODEL_ATTRIBUTE_NAME, form);
        }

        return "select";
    }

    @PostMapping(value = "/{reportId:[\\d]+}/save", params = "save")
    public String saveFormSelection(@PathVariable Long reportId,
                                    @ModelAttribute ComposeReportForm form,
                                    BindingResult result,
                                    Map<String, Object> model) {
        form = setupComposeReportForm(reportId, form);
        List<ReportingFormType> selectedForms = form.bindSelectedForms();

        if (selectedForms.isEmpty()) {
            result.reject("forms.selection.error");

            return "select";
        }

        Report report = form.getReport();
        report.setSelectedForms(selectedForms);
        reportService.saveReport(report);

        return "redirect:/" + reportId + "/list";
    }

    @PostMapping(value = "/{reportId:[\\d]+}/save", params = "upload")
    public String upload(@PathVariable Long reportId,
                         @ModelAttribute ComposeReportForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        form = setupComposeReportForm(reportId, form);
        Report report = form.getReport();
        List<ReportingFormType> selectedForms = form.bindSelectedForms();

        report.setSelectedForms(selectedForms);
        form.setXbrlInstance(xbrlService.createInstanceModel(report));

        if (!form.getInstanceFile().isEmpty()) {
            try {
                XbrlInstance uploadedInstance = xbrlService.read(form.getInstanceFile().getBytes());
                TransformResult transformResult = xbrlService.transform(uploadedInstance, form.getXbrlInstance());

                if (!transformResult.getXbrlErrors().isEmpty()) {
                    for (XbrlError error : transformResult.getXbrlErrors()) {
                        result.reject(error.getCode() + ".nolink", error.getArguments(), error.getCode() + ".nolink");
                    }
                }

                form.setXbrlInstance(transformResult.getXbrlInstance());
            } catch (Exception e) {
                if(e.getCause() instanceof XbrlEngineException) {
                    XbrlEngineException ex = (XbrlEngineException) e.getCause();
                    if (ex.getError() != null) {
                        result.reject(ex.getError().getCode(), ex.getError().getArguments(), ex.getError().getCode());
                    } else {
                        result.reject("xbrl.upload.error");
                    }
                } else {
                    result.reject("xbrl.upload.error");
                }
            }
        } else {
            result.reject("xbrl.upload.error");
        }

        reportService.saveReport(form.getReport());
        reportService.saveReportFields(form.getReport(), form.getXbrlInstance());

        // If we have errors, then before redirect store them in flash attr, so that they dont get lost
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute(ComposeReportForm.MODEL_ATTRIBUTE_NAME, form);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + ComposeReportForm.MODEL_ATTRIBUTE_NAME, result);
        }

        return "redirect:/" + reportId;
    }

    @RequestMapping(value = "/{reportId:[\\d]+}/list")
    public String formList(@PathVariable Long reportId,
                           Map<String, Object> model) {
        ReportForm form = setupReportForm(reportId);
        model.put(ReportForm.MODEL_ATTRIBUTE_NAME, form);

        return "list";
    }

    @RequestMapping(value = "/{reportId:[\\d]+}/{formCode}")
    public String fillForm(@PathVariable Long reportId,
                           @PathVariable String formCode,
                           Map<String, Object> model) {
        ReportingFormType formType = reportService.getReportingFormTypeByCode(formCode);
        ReportForm form = setupReportForm(reportId);

        model.put(ReportForm.MODEL_ATTRIBUTE_NAME, form);
        model.put(CURRENT_FORM, getReportingFormByCode(formType.getCode(), form.getReport().getSelectedForms()));
        model.put(FORM_METAMODEL, xbrlService.getReportingFormMetamodel(formType.getCode()));
        model.put(CONTEXTS, xbrlService.getVisibleFormContexts(form.getReport(), formType));

        return "form";
    }

    @PostMapping(value = "/{reportId:[\\d]+}/{formCode}/save", params = "save")
    public String saveForm(@PathVariable Long reportId,
                           @PathVariable String formCode,
                           @ModelAttribute ReportForm form,
                           BindingResult result,
                           Map<String, Object> model) {
        ReportingFormType formType = reportService.getReportingFormTypeByCode(formCode);
        form = setupReportForm(reportId, form);
        form.setXbrlInstance(reportService.saveReportFields(form.getReport(), form.getXbrlInstance()));

        HashSet<XbrlError> errors = new HashSet<>(xbrlService.validate(form.getReport(), form.getXbrlInstance(), formType, LocaleContextHolder.getLocale()));

        if (!errors.isEmpty()) {
            for (XbrlError error : errors) {
                result.reject(error.getCode() + ".nolink", error.getArguments(), error.getCode() + ".nolink");
            }

            model.put(ReportForm.MODEL_ATTRIBUTE_NAME, form);
            model.put(CURRENT_FORM, getReportingFormByCode(formType.getCode(), form.getReport().getSelectedForms()));
            model.put(FORM_METAMODEL, xbrlService.getReportingFormMetamodel(formType.getCode()));
            model.put(CONTEXTS, xbrlService.getVisibleFormContexts(form.getReport(), formType));

            return "form";
        }

        return "redirect:/" + reportId + "/list";
    }

    @PostMapping(value = "/{reportId:[\\d]+}/{formCode}/save", params = "recalculate")
    public String recalculateForm(@PathVariable Long reportId,
                                  @PathVariable String formCode,
                                  @ModelAttribute ReportForm form,
                                  BindingResult result,
                                  Map<String, Object> model) {
        ReportingFormType formType = reportService.getReportingFormTypeByCode(formCode);
        form = setupReportForm(reportId, form);

        try {
            form.setXbrlInstance(xbrlService.calculate(xbrlService.getReportingFormMetamodel(formType.getCode()), form.getXbrlInstance()));
            model.put("message", messageSource.getMessage("xbrl.recalculate.success", new Object[]{}, LocaleContextHolder.getLocale()));
        } catch (XbrlEngineException e) {
            result.reject("xbrl.save.error");
        } catch (Exception e) {
            result.reject("xbrl.save.error.empty");
        }

        model.put(ReportForm.MODEL_ATTRIBUTE_NAME, form);
        model.put(CURRENT_FORM, getReportingFormByCode(formType.getCode(), form.getReport().getSelectedForms()));
        model.put(FORM_METAMODEL, xbrlService.getReportingFormMetamodel(formType.getCode()));
        model.put(CONTEXTS, xbrlService.getVisibleFormContexts(form.getReport(), formType));

        return "form";
    }

    @RequestMapping("/{reportId:[\\d]+}/download")
    public ResponseEntity<Resource> download(@PathVariable Long reportId) {
        Report report = reportService.getReport(reportId);
        byte[] xbrlBytes = xbrlService.write(xbrlService.getPopulatedXbrlInstance(report));

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=report.xbrl")
                .contentLength(xbrlBytes.length)
                .contentType(MediaType.parseMediaType("application/xml"))
                .body(new ByteArrayResource(xbrlBytes));
    }

    @RequestMapping("/{reportId:[\\d]+}/validate")
    public String validate(@PathVariable Long reportId,
                           RedirectAttributes redirectAttributes) {
        HashSet<XbrlError> errors = new HashSet<>();
        Report report = reportService.getReport(reportId);
        XbrlInstance xbrlInstance = xbrlService.getPopulatedXbrlInstance(report);

        for (ReportingFormType formType : report.getSelectedForms()) {
            List<XbrlError> formErrors = new ArrayList<>();

            try {
                formErrors = xbrlService.validate(report, xbrlInstance, formType, LocaleContextHolder.getLocale());
            } catch (XbrlEngineException e) {
                log.error("Form " + formType.getCode() + " validation caused an exception " + e);
            }

            if (!formErrors.isEmpty()) {
                errors.addAll(formErrors);
            }
        }

        if (!errors.isEmpty()) {
            redirectAttributes.addFlashAttribute("xbrlErrors", errors);
        }

        return "redirect:/" + reportId + "/list";
    }

    private ReportingFormType getReportingFormByCode(String code, List<ReportingFormType> selectedForms){
        for (ReportingFormType form : selectedForms){
            if (form.getCode().equals(code)){
                return form;
            }
        }
        return null;
    }

    private ComposeReportForm setupComposeReportForm(Long reportId) {
        ComposeReportForm form = new ComposeReportForm();

        return setupComposeReportForm(reportId, form);
    }

    private ComposeReportForm setupComposeReportForm(Long reportId, ComposeReportForm form) {
        Report report = reportService.getReport(reportId);

        if (form.getReport() != null) {
            report.setMultiplier(form.getReport().getMultiplier());
        }

        form.setReport(report);
        // NB! You should actually get a list of forms depending on client type or some other criteria.
        form.setAvailableForms(reportService.getListOfReportingFormTypes());

        if (form.getSelectedFormCodes() == null) {
            form.createEmptySelectedCodes();
        }

        initializeReport(form, report);

        form.setXbrlInstance(xbrlService.getPopulatedXbrlInstance(report));

        List<ReportingFormMetamodel> metamodels = xbrlService.getReportingFormMetamodels(form.getAvailableForms());
        form.setReportingFormMetamodels(metamodels);

        for (ReportingFormType formType : form.getAvailableForms()) {
            formType.setContainsData(xbrlService.reportingFormContainsData(report, form.getXbrlInstance(), formType));
        }

        return form;
    }

    private void initializeReport(ComposeReportForm form, Report report) {
        List<ReportingFormType> selectedForms = report.getSelectedForms();

        if (selectedForms == null || selectedForms.isEmpty() || report.getMultiplier() == null){
            List<StatementType> statementTypes = form.getStatementTypes();
            selectedForms = new ArrayList<>();

            for (StatementType statementType : statementTypes){
                selectedForms.add(form.getAvailableForms(statementType).get(0));
            }

            report.setMultiplier(XbrlValueEntry.Multiplier.ONE);

            //Selected forms is set to all available forms to pre fill data for any of the available forms
            report.setSelectedForms(form.getAvailableForms());
            preFillReportWithDataFromPreviousReport(report);

            //After any found data has been saved, selected forms is set to actual initial set of selected forms
            report.setSelectedForms(selectedForms);
            reportService.saveReport(report);
        }
    }

    private void preFillReportWithDataFromPreviousReport(Report report) {
        int previousAccountingYear = report.getAccountingYear() - 1;
        Report previousReport = null; // TODO Find report for previousAccountingYear for current user

        if (previousReport != null) {
            XbrlInstance previousInstance = xbrlService.getPopulatedXbrlInstance(previousReport);
            XbrlInstance currentInstance = xbrlService.createInstanceModel(report);
            XbrlInstance preFilledInstance = xbrlService.transform(previousInstance, currentInstance).getXbrlInstance();

            reportService.saveReportFields(report, preFilledInstance);
        }
    }

    private ReportForm setupReportForm(Long reportId) {
        ReportForm form = new ReportForm();

        return setupReportForm(reportId, form);
    }

    private ReportForm setupReportForm(Long reportId, ReportForm form) {
        Report report = reportService.getReport(reportId);
        form.setReport(report);
        form.setXbrlInstance(xbrlService.getPopulatedXbrlInstance(report));

        for (ReportingFormType formType : form.getReport().getSelectedForms()) {
            formType.setContainsData(xbrlService.reportingFormContainsData(report, form.getXbrlInstance(), formType));
        }

        List<ReportingFormMetamodel> metamodels = xbrlService.getReportingFormMetamodels(report.getSelectedForms());
        form.setReportingFormMetamodels(metamodels);

        // Fill xbrl instance with data posted by user and unset it
        if (form.hasValues()) {
            form.getXbrlInstance().getDisplayValues().putAll(form.getValues());
            form.setValues(null);
        }

        return form;
    }

}
