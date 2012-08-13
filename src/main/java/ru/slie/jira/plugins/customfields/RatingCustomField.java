package ru.slie.jira.plugins.customfields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.impl.SelectCFType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import java.util.List;
import java.util.Map;
import org.ofbiz.core.entity.GenericValue;

public class RatingCustomField extends SelectCFType {
    //private static final Logger log = LoggerFactory.getLogger(StarsCustomField.class);
    public RatingCustomField(CustomFieldValuePersister customFieldValuePersister,
            OptionsManager optionManager,
            GenericConfigManager genericConfigManager,
            JiraBaseUrls jiraBaseUrls ) {
        super(customFieldValuePersister, optionManager, genericConfigManager, jiraBaseUrls);
    }
    
    @Override
    public Map<String, Object> getVelocityParameters(final Issue issue,
                                                     final CustomField field,
                                                     final FieldLayoutItem fieldLayoutItem) {
        final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);
        // This method is also called to get the default value, in
        // which case issue is null so we can't use it to add currencyLocale
        if (issue == null) {
            return map;
        }
        OptionsManager optionsManager = ComponentManager.getComponentInstanceOfType(OptionsManager.class);
        FieldConfig fieldConfig = field.getRelevantConfig(issue);
        map.put("options", optionsManager.getOptions(fieldConfig));
        return map;
    }
    
    
    @Override
    public Option getValueFromIssue(CustomField field, Issue issue) {
        final Option option = super.getValueFromIssue(field, issue);
        if (option == null)
        {
            return new Option() {

                @Override
                public Long getOptionId() {
                    return 99999999999L;
                }

                @Override
                public Long getSequence() {
                    return 0L;
                }

                @Override
                public String getValue() {
                    return "None";
                }

                @Override
                public Boolean getDisabled() {
                    return false;
                }

                @Override
                public GenericValue getGenericValue() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public FieldConfig getRelatedCustomField() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Option getParentOption() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<Option> getChildOptions() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setSequence(Long l) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setValue(String string) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void setDisabled(Boolean bln) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<Option> retrieveAllChildren(List<Option> list) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void store() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };            
        }
        return option;
    }
}