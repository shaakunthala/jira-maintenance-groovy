/*
    Author:       Sameera Shaakunthala
    Description:  If you use JIRA Workflow Toolbox add-on, this will help you to
                  display a summary of all calculated number custom fields used
                  in your JIRA instance. Useful for documentation purposes.
                  At the moment, if your custom fields have multiple contexts, 
                  only the first context configuration will be shown.
*/

import com.atlassian.jira.component.ComponentAccessor
import org.apache.commons.lang.StringEscapeUtils
import com.atlassian.jira.issue.context.*
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory
import com.atlassian.sal.api.pluginsettings.PluginSettings
import com.atlassian.jira.issue.issuetype.IssueType
import com.atlassian.jira.issue.fields.CustomField

def customFieldManager = ComponentAccessor.getCustomFieldManager()
def fieldConfigSchemeManager = ComponentAccessor.getFieldConfigSchemeManager()
def issueTypes = ComponentAccessor.getConstantsManager().getAllIssueTypeObjects()
PluginSettingsFactory pluginSettingsFactory = (PluginSettingsFactory) ComponentAccessor.getOSGiComponentInstanceOfType( PluginSettingsFactory.class )
def jwtKey = "com.fca.jira.plugins.workflowToolbox"
AbstractJiraContext globalContext = new GlobalIssueContext()

def calcFieldConfigToHtml (PluginSettings pconfig) {
    def expression = pconfig.get("expression")
    def displayFormatFormatType = pconfig.get("displayFormatFormatType")
    def displayFormatDurationType = pconfig.get("displayFormatDurationType")
    def displayFormatUseTimeTrackingSettings = pconfig.get("displayFormatUseTimeTrackingSettings")
    def displayFormatNumberFormat = pconfig.get("displayFormatNumberFormat")
    def displayFormatHideZeros = pconfig.get("displayFormatHideZeros")
    def ret = []
    ret << ( expression != null ? "Expression: <pre style=\"border: 1px dashed black; padding: 5px;\">" + expression + "</pre>" : "" )
    ret << ( displayFormatFormatType != null ? "Display format type: " + displayFormatFormatType : "" )
    ret << ( displayFormatDurationType != null ? "Display format duration type: " + displayFormatDurationType : "" )
    ret << ( displayFormatUseTimeTrackingSettings != null ? "Display format - use time tracking settings: " + displayFormatUseTimeTrackingSettings : "" )
    ret << ( displayFormatNumberFormat != null ? "Display number format: " + displayFormatNumberFormat : "" )
    ret << ( displayFormatHideZeros != null ? "Uninitialized when value is 0: " + displayFormatHideZeros : "" )
    return ( ret.join ("") != "" ? ret.grep{ it != "" }.join ("<br />") : "Not configured!" )
}

def customFields = customFieldManager.getCustomFieldObjects().grep { CustomField customField ->
    customField.getCustomFieldType().descriptor.key == "calculated-number-field"
}.collect { CustomField customField ->
    def config = ""
    def pconfig = null
    def fieldConfig = null
    def expression = ""
    def format = ""
    def contexts = issueTypes.collect {
        IssueContext co = new IssueContextImpl ( null, (IssueType)it )
        if ( customField.isRelevantForIssueContext (co) ) return co
    }.grep ()
    if (customField.isGlobal()) {
        fieldConfig = fieldConfigSchemeManager.getRelevantConfig(globalContext, customField)
        pconfig = pluginSettingsFactory.createSettingsForKey( jwtKey + "@" + customField.getIdAsLong() + "@" + fieldConfig.id )
        config = calcFieldConfigToHtml (pconfig)
    } else {
        if (contexts != null) {
            fieldConfig = fieldConfigSchemeManager.getRelevantConfig((IssueContext) contexts.first(), customField)
            pconfig = pluginSettingsFactory.createSettingsForKey( jwtKey + "@" + customField.getIdAsLong() + "@" + fieldConfig.id )
            config = calcFieldConfigToHtml (pconfig)
        } else {
            config = "No custom field contexts!"
        }
    }
    
    [
        customField.name, 
        ( customField.description ? StringEscapeUtils.escapeHtml(customField.description) : "" ), 
        customField.getCustomFieldType().name, 
        config
    ]
}.sort { it[0] }

def html = "-- begining: text marker to help copy-pasting into a document--<table id=\"groovy-result-table\" border=\"1\" bordercolor=\"#fff\" cellspacing=\"0\" cellpadding=\"5\">"
html = html + "<tr><th>" + [
    "Name", 
    "Description", 
    "Type", 
    "Calculated custom field configuration"
].join ("</th><th>") + "</th></tr>"
html = html + customFields.collect {
    "<td>" + it.join ("</td><td>") + "</td>"
}.join ("</tr><tr>") + "</tr></table>-- end: text marker to help copy-pasting into a document--"
html = html + "<script>AJS.\$(\"table#groovy-result-table tr\").each(function(e,r){var c=\"#e5e5e5\";e%2==1&&(c=\"#c6d3e4\"),AJS.\$(r).children(\"td\").css(\"background-color\",c)});</script>"
