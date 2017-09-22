<#import "/spring.ftl" as spring />

<#macro field reportForm presentationEntry reportMetamodel valueEntry totalInput lang>
    <#assign collision_tooltip="">
    <#list reportForm.report.selectedForms as form>
        <#if presentationEntry.ancestors?has_content && presentationEntry.ancestors?seq_contains(form.code) && form.code != reportMetamodel.code>
            <#if collision_tooltip?has_content>
                <#assign collision_tooltip="${collision_tooltip},">
            </#if>
            <#assign collision_tooltip="${collision_tooltip} [${form.code}] ${reportForm.getReportingFormName(form.code, lang)!}">
        </#if>
    </#list>

    <#if reportForm.report.multiplier == 'ONE'>
        <#assign multiplierClass="one" />
    </#if>
    <#if reportForm.report.multiplier == 'THOUSAND'>
        <#assign multiplierClass="thousand" />
    </#if>

    <span class="${collision_tooltip?has_content?then('collision-field', '')}">
        <#assign collisionExists=collision_tooltip?has_content />
        <#assign fieldOnAnotherForms><@spring.message "field.appears.on.another.forms"/></#assign>
        <#assign fieldOnOtherForms><@spring.message "field.appears.on.other.forms"/></#assign>
        <#switch presentationEntry.type>
            <#case 'TEXT_BLOCK'>
                <@spring.formTextarea "reportForm.values['${valueEntry.id}']"
                    "class='form-control textblock-field'
                    maxlength='2000'
                    data-html='true'
                    data-toggle='${collisionExists?then('popover', '')}'
                    data-content=\"${collisionExists?then(fieldOnAnotherForms + ': ' + collision_tooltip, '')}\"" />
                <#break>
            <#case 'STRING'>
                <@spring.formInput "reportForm.values['${valueEntry.id}']"
                    "class='form-control input-sm string-field'
                    data-html='true'
                    data-toggle='${collisionExists?then('popover', '')}'
                    data-content=\"${fieldOnAnotherForms}: ${collision_tooltip}\"" />
                <#break>
            <#case 'DATE'>
                <div class="input-group date input-group-date pull-right">
                        <span class="input-group-addon">
                            <i class="glyphicon glyphicon-calendar"></i>
                        </span>
                    <@spring.formInput "reportForm.values['${valueEntry.id}']"
                        "class='form-control input-sm date-field'
                        data-html='true'
                        data-toggle='${collisionExists?then('popover', '')}'
                        data-content=\"${fieldOnAnotherForms}: ${collision_tooltip}\"" />
                    </div>
                <#break>
            <#case 'PER_SHARE'>
                <@spring.formInput "reportForm.values['${valueEntry.id}']"
                    "class='form-control input-sm monetary-field pershare-field ${totalInput?then('total_input','')}'
                    data-html='true'
                    data-toggle='${collisionExists?then('popover', '')}'
                    data-content=\"${fieldOnAnotherForms}: ${collision_tooltip}\"" />
                <#break>
            <#default>
                <@spring.formInput "reportForm.values['${valueEntry.id}']"
                    "class='form-control input-sm monetary-field monetary-field-${multiplierClass} ${totalInput?then('total_input','')}'
                    data-html='true'
                    data-toggle='${collisionExists?then('popover', '')}'
                    data-content=\"${fieldOnAnotherForms}: ${collision_tooltip}\"" />
        </#switch>
    </span>
</#macro>

<#macro presentationEntryReference presentationEntry lang>
    <#if presentationEntry.labels[lang].documentationLabel?has_content>
        <#assign references="">
        <#list presentationEntry.references as reference>
            <#if references?has_content>
                <#assign references="${references}, ">
            </#if>
            <#assign issueDate=reference.issueDate?string['yyyy-MM-dd']>
            <#if reference.uri?has_content>
                <#assign references="${references} ${reference.name} ${reference.number!}
                        ${issueDate} §${reference.paragraph!} ${reference.subParagraph!}"/>
            <#else>
                <#assign references="${references} <a target='_blank' href='${reference.uri}'>${reference.name} ${reference.number!}
                        ${issueDate} §${reference.paragraph!} ${reference.subParagraph!}</a>"/>
            </#if>
        </#list>

    <#assign referenceSeeMessage><@spring.message "reference.see"/></#assign>
    <#assign content=presentationEntry.labels[lang].documentationLabel + references?has_content?then(referenceSeeMessage + references, '')>
    <a class="btn btn-default btn-sm form-control-help" data-html="true" data-toggle="popover" data-content="${content}">
        <span class="fa fa-question"></span>
    </a>
    </#if>
</#macro>

<#macro flatForm reportForm formMetamodel contexts lang>
    <#assign instance = reportForm.xbrlInstance>
    <#assign presentationEntries = formMetamodel.getFlatPresentation()>

    <table class="table table-hover">
        <thead>
            <tr>
                <th></th>
                <th></th>
                <#list contexts as context>
                    <#if !context.hasExplicitMember()>
                        <th class="text-center">
                            <#if context.periodLabel?has_content>
                                ${context.periodLabel}
                            <#else>
                                <@spring.message "context.period.label.forever"/>
                            </#if>
                        </th>
                    </#if>
                </#list>
            </tr>
        </thead>
        <tbody>
            <#assign count = 0>
            <#list presentationEntries as presentationEntry>
                <#assign colspan = presentationEntry.isAbstract()?then(contexts?size + 2, 1)>
                <tr>
                    <td colspan="${colspan}">
                        <#assign isTotal = formMetamodel.isTotalPresentation(presentationEntry)?then(true,false)>
                        <div class="level_${presentationEntry.level} ${isTotal?then('total_label', '')} ${presentationEntry.isAbstract()?then('level_abstract', '')}">
                            <#if isTotal>
                                <span class="req">*</span>
                            </#if>
                            ${presentationEntry.getPreferredLabel(lang)}
                        </div>
                    </td>
                    <#if !presentationEntry.isAbstract()>
                        <td>
                            <@presentationEntryReference presentationEntry=presentationEntry lang=lang />
                        </td>
                        <#list contexts as context>
                            <#if !context.hasExplicitMember()>
                                <#assign areaClass = (presentationEntry.type=='TEXT_BLOCK')?then('area-cell', '')>
                                <td class="${areaClass}">
                                    <#if !presentationEntry.isAbstract()>
                                        <#assign valueEntry = instance.getValue(context, presentationEntry)>
                                        <@field reportForm=reportForm
                                            presentationEntry=presentationEntry
                                            reportMetamodel=formMetamodel
                                            valueEntry=valueEntry
                                            totalInput=formMetamodel.isTotalPresentation(presentationEntry)
                                            lang=lang/>
                                    </#if>
                                </td>
                                <#assign count = count + 1>
                            </#if>
                        </#list>
                    </#if>
                </tr>
            </#list>
        </tbody>
    </table>
</#macro>

<#macro hypercube reportForm context presentationEntry reportMetamodel lang=lang>
    <#assign dimension=reportMetamodel.getDimension(presentationEntry)>
    <#assign table=presentationEntry.children[0]>
    <#assign tableDimension=dimension.getChild(table)>
    <#assign lineItems=presentationEntry.children[1]>
    <#assign lineItemsDimension=dimension.getChild(table)>
    <#assign axis=table.children[0]>
    <#assign axisDimension=tableDimension.getChild(axis)>

    <table class="table table-hover">
        <thead>
        <tr>
            <th colspan="2"></th>
            <@hypercubeAxis axis=axis dimension=axisDimension lang=lang/>
        </tr>
        </thead>
        <tbody>
            <@hypercubeMember reportForm=reportForm context=context axis=axis
                                  axisDimension=axisDimension lineItem=lineItems reportMetamodel=reportMetamodel lang=lang/>
        </tbody>
    </table>
</#macro>

<#macro hypercubeAxis axis dimension lang>
    <#if axis.children?has_content>
        <#list axis.children as childAxis>
            <@hypercubeAxis axis=childAxis dimension=dimension.getChild(childAxis) lang=lang />
        </#list>
    </#if>

    <#if dimension.arcRole == 'DOMAIN_MEMBER' || dimension.arcRole == 'DIMENSION_DOMAIN'>
        <th class="text-center col-total">
            ${axis.getPreferredLabel(lang)}
        </th>
    </#if>
</#macro>

<#macro hypercubeMember reportForm context axis axisDimension lineItem reportMetamodel lang>
    <#assign isTotal=reportMetamodel.isTotalPresentation(lineItem)?then(true, false)>
    <tr>
        <td colspan="${lineItem['abstract']?then(axisDimension.getDomainElementCount() + 2, '1')}">
            <div class="level_${lineItem.level} ${lineItem['abstract']?then('level_abstract', '')} ${isTotal?then('total_label', '')}">
                <#if isTotal>
                    <span class="req">*</span>
                </#if>
                ${lineItem.getPreferredLabel(lang)}
                <#if lineItem.period == 'INSTANT'>
                    <#if lineItem.preferredLabelType == 'PERIOD_START'>
                        ${context.getPeriodStartDate()?string['yyyy-MM-dd']}
                    </#if>
                    <#if lineItem.preferredLabelType == 'PERIOD_END'>
                        ${context.getPeriodEndDate()?string['yyyy-MM-dd']}
                    </#if>
                </#if>
            </div>
        </td>
        <#if !lineItem['abstract']>
            <td>
                <@presentationEntryReference presentationEntry=lineItem lang=lang />
            </td>
            <@hypercubeMemberAxis reportForm=reportForm context=context axis=axis
                                    lineItem=lineItem reportMetamodel=reportMetamodel lang=lang/>
        </#if>
    </tr>
    <#if lineItem.children?has_content>
        <#list lineItem.children as lineItem>
            <@hypercubeMember reportForm=reportForm context=context axis=axis
                                  axisDimension=axisDimension lineItem=lineItem reportMetamodel=reportMetamodel lang=lang/>
        </#list>
    </#if>
</#macro>

<#macro hypercubeMemberAxis reportForm context axis lineItem reportMetamodel lang>
    <#assign instance=reportForm.xbrlInstance>

    <#list axis.children as child>
        <@hypercubeMemberAxis reportForm=reportForm context=context axis=child
                                lineItem=lineItem reportMetamodel=reportMetamodel lang=lang/>
    </#list>
    <#if lineItem['abstract'] && axis.type != 'AXIS'>
        <td></td>
    </#if>
    <#if axis.type != 'AXIS'>
        <td>
            <#assign dimension=reportMetamodel.getDimension(axis)>
            <#assign valueEntry=instance.getValue(context, lineItem, dimension)>
            <#assign totalInput=(reportMetamodel.isTotalPresentation(lineItem) || dimension.arcRole == 'DIMENSION_DOMAIN')>
            <@field reportForm=reportForm presentationEntry=lineItem reportMetamodel=reportMetamodel
                        valueEntry=valueEntry totalInput=totalInput lang=lang/>
        </td>
    </#if>
</#macro>