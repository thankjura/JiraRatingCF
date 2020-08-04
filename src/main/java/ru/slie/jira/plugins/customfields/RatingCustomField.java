package ru.slie.jira.plugins.customfields;

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
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.ofbiz.core.entity.GenericValue;
import ru.slie.jira.plugins.customfields.admin.RatingCustomFieldSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RatingCustomField extends SelectCFType {
	private final OptionsManager optionsManager;
	private final RatingCustomFieldSettings settings;

	public RatingCustomField(@ComponentImport CustomFieldValuePersister customFieldValuePersister,
							 @ComponentImport OptionsManager optionsManager,
							 @ComponentImport GenericConfigManager genericConfigManager,
							 @ComponentImport JiraBaseUrls jiraBaseUrls,
							 RatingCustomFieldSettings settings) {
		super(customFieldValuePersister, optionsManager, genericConfigManager, jiraBaseUrls);
		this.optionsManager = optionsManager;
		this.settings = settings;
	}

	@Override
	public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
		final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

		if (issue == null) {
			return map;
		}
		FieldConfig fieldConfig = field.getRelevantConfig(issue);

		map.put("options", optionsManager.getOptions(fieldConfig));
		return map;
	}

	@Override
	public Option getValueFromIssue(final CustomField field, final Issue issue) {
		final Option option = super.getValueFromIssue(field, issue);
		if (option == null && settings.isShowEmptyField()) {
			return new Option() {
				@Override
				public Long getOptionId() {
					return -1L;
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
					return null;
				}

				@Override
				public FieldConfig getRelatedCustomField() {
					if (issue != null) {
						return field.getRelevantConfig(issue);
					}

					return null;
				}

				@Override
				public Option getParentOption() {
					return null;
				}

				@Override
				public List<Option> getChildOptions() {
					return new ArrayList<>();
				}

				@Override
				public void setSequence(Long l) {
				}

				@Override
				public void setValue(String string) {
				}

				@Override
				public void setDisabled(Boolean bln) {
				}

				@Override
				public List<Option> retrieveAllChildren(List<Option> list) {
					return null;
				}

				@Override
				public void store() {
				}
			};
		}
		return option;
	}
}
