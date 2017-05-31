package ru.slie.jira.plugins.customfields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.SelectCustomFieldImporter;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.AbstractSingleFieldType;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.customfields.impl.rest.SelectCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.*;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.CustomFieldOptionJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Select Custom Field Type allows selecting of a single {@link Option}.
 * <em>Transport Object</em> is {@link Option}
 *
 * dl> <dt><strong>Transport Object Type</strong></dt> <dd>{@link Option}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt> <dd>{@link String} of Option
 * ID</dd> </dl>
 */
public class RatingCustomField extends AbstractSingleFieldType<Option> implements
		MultipleSettableCustomFieldType<Option, Option>,
		SortableCustomField<String>,
		GroupSelectorField,
		ProjectImportableCustomField,
		RestAwareCustomFieldType,
		RestCustomFieldTypeOperations {
	private final OptionsManager optionsManager;
	private final ProjectCustomFieldImporter projectCustomFieldImporter;
	private final JiraBaseUrls jiraBaseUrls;
	private static final Long PHANTOM_OPTION = 99999999999L;

	// private static final Logger log =
	// Logger.getLogger(RatingCustomField.class);

	public RatingCustomField(CustomFieldValuePersister customFieldValuePersister,
							 OptionsManager optionsManager,
							 GenericConfigManager genericConfigManager,
							 JiraBaseUrls jiraBaseUrls) {
		super(customFieldValuePersister, genericConfigManager);
		this.optionsManager = optionsManager;
		this.jiraBaseUrls = jiraBaseUrls;
		this.projectCustomFieldImporter = new SelectCustomFieldImporter();
	}

	@Override
	public Set<Long> remove(final CustomField field) {
		final Set<Long> issues = super.remove(field);
		optionsManager.removeCustomFieldOptions(field);
		return issues;
	}

	@Nonnull
	@Override
	protected PersistenceFieldType getDatabaseType() {
		return PersistenceFieldType.TYPE_LIMITED_TEXT;
	}

	@Override
	protected Object getDbValueFromObject(Option customFieldObject) {
		return getStringFromSingularObject(customFieldObject);
	}

	@Override
	protected Option getObjectFromDbValue(@Nonnull Object databaseValue) throws FieldValidationException {
		return getSingularObjectFromString((String) databaseValue);
	}

	/**
	 * This default implementation will remove all values from the custom field
	 * for an issue. Since there can only be one value for each CustomField
	 * instance, this implementation can safely ignore the objectValue
	 *
	 * @param option
	 *            - ignored
	 */
	@Override
	public void removeValue(final CustomField field, final Issue issue, final Option option) {
		updateValue(field, issue, null);
	}

	@Override
	public Option getSingularObjectFromString(final String string) throws FieldValidationException {
		if ("-1".equals(string)) {
			return null;
		}

		if (PHANTOM_OPTION.toString().equals(string)) {
			return null;
		}

		return getOptionFromStringValue(string);
	}

	private Option getOptionFromStringValue(String selectValue) throws FieldValidationException {
		final Long aLong = OptionUtils.safeParseLong(selectValue);
		if (aLong != null) {
			final Option option = optionsManager.findByOptionId(aLong);
			if (option != null) {
				return option;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public String getStringFromSingularObject(final Option optionObject) {
		if (optionObject == null) {
			return null;
		} else {
			if (PHANTOM_OPTION.equals(optionObject.getOptionId())) {
				return null;
			}
		}

		Long option = optionObject.getOptionId();
		if (option != null) {
			return option.toString();
		}

		return null;
	}

	public Set<Long> getIssueIdsWithValue(final CustomField field, final Option option) {
		if (option != null) {
			return customFieldValuePersister.getIssueIdsWithValue(field, PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
		} else {
			return Collections.emptySet();
		}
	}

	@Nonnull
	@Override
	public List<FieldConfigItemType> getConfigurationItemTypes() {
		final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
		configurationItemTypes.add(new SettableOptionsConfigItem(this, optionsManager));
		return configurationItemTypes;
	}

	@Override
	public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config) {
		final String selectedString = (String) relevantParams .getFirstValueForNullKey();

		if (StringUtils.isNotBlank(selectedString) && !"-1".equals(selectedString)) {
			// Test to see if the non blank value exists in the options
			final Options options = optionsManager.getOptions(config);
			final CustomField customField = config.getCustomField();
			final String validOptions = createValidOptionsString(options);
			Long optionId = null;
			try {
				optionId = Long.valueOf(selectedString);
			} catch (NumberFormatException e) {
				errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText(
												"admin.errors.invalid.value.passed.for.customfield",
												"'" + selectedString + "'",
												"'" + customField + "'",
												validOptions), Reason.VALIDATION_FAILED);
			}
			if ((options != null) && (options.getOptionById(optionId) == null)) {
				errorCollectionToAddTo.addError(customField.getId(), getI18nBean().getText(
												"admin.errors.invalid.value.passed.for.customfield",
												"'" + selectedString + "'",
												"'" + customField + "'",
												validOptions), Reason.VALIDATION_FAILED);
			}
		}

	}

	public void setDefaultValue(final FieldConfig fieldConfig, final Option option) {
		Long id = null;
		if (option != null) {
			id = option.getOptionId();
			if (PHANTOM_OPTION.equals(id)) {
				id = null;
			}
		}
		genericConfigManager.update(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), id);
	}

	public Option getDefaultValue(final FieldConfig fieldConfig) {
		Long optionId = (Long) genericConfigManager.retrieve(CustomFieldType.DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
		if (optionId == null) {
			return null;
		}
		return optionsManager.findByOptionId(optionId);
	}

	@Override
	public String getChangelogString(CustomField field, Option value) {
		return value == null ? null : value.getValue();
	}

	private String createValidOptionsString(final Options options) {
		final List<Option> rootOptions = options.getRootOptions();
		final StringBuilder validOptions = new StringBuilder();

		for (Iterator<Option> optionIterator = rootOptions.iterator(); optionIterator.hasNext();) {
			Option option = optionIterator.next();
			validOptions.append(option.getOptionId()).append("[").append(option.getValue()).append("]");

			if (optionIterator.hasNext()) {
				validOptions.append(", ");
			}
		}
		validOptions.append(", -1");
		return validOptions.toString();
	}

	// -----------------------------------------------------------------------------
	// MultiSettable Methods
	public Options getOptions(final FieldConfig config,	@Nullable final JiraContextNode jiraContextNode) {
		return optionsManager.getOptions(config);
	}

	@Override
	public ProjectCustomFieldImporter getProjectImporter() {
		return projectCustomFieldImporter;
	}

	public Query getQueryForGroup(final String fieldID, String groupName) {
		return new TermQuery(new Term(fieldID + SelectStatisticsMapper.RAW_VALUE_SUFFIX, groupName));
	}

	@Override
	public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext) {
		// Get the allowed options
		FieldConfig config = ((CustomField) fieldTypeInfoContext.getOderableField()).getRelevantConfig(fieldTypeInfoContext.getIssueContext());
		Options options = optionsManager.getOptions(config);
		Collection<CustomFieldOptionJsonBean> optionBeans = CustomFieldOptionJsonBean.shortBeans(options, jiraBaseUrls);

		return new FieldTypeInfo(optionBeans, null);
	}

	@Override
	public JsonType getJsonSchema(CustomField customField) {
		return JsonTypeBuilder.custom(JsonType.STRING_TYPE, getKey(), customField.getIdAsLong());
	}

	@Override
	public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem) {
		Option valueFromIssue = getValueFromIssue(field, issue);
		if (valueFromIssue == null) {
			return new FieldJsonRepresentation(new JsonData(null));
		}
		return new FieldJsonRepresentation(new JsonData(CustomFieldOptionJsonBean.shortBean(valueFromIssue, jiraBaseUrls)));
	}

	@Override
	public RestFieldOperationsHandler getRestFieldOperation(CustomField field) {
		return new SelectCustomFieldOperationsHandler(optionsManager, field, getI18nBean());
	}

	@Override
	public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field) {
		FieldConfig config = field.getRelevantConfig(issueCtx);
		Option defaultValue = (Option) field.getCustomFieldType().getDefaultValue(config);
		return defaultValue == null ? null : new JsonData(CustomFieldOptionJsonBean.shortBean(defaultValue, jiraBaseUrls));
	}

	@Override
	public Map<String, Object> getVelocityParameters(final Issue issue, final CustomField field, final FieldLayoutItem fieldLayoutItem) {
		final Map<String, Object> map = super.getVelocityParameters(issue, field, fieldLayoutItem);

		// This method is also called to get the default value, in
		// which case issue is null so we can't use it to add currencyLocale
		if (issue == null) {
			return map;
		}
		OptionsManager optionsManager = getOptionsManager();
		FieldConfig fieldConfig = field.getRelevantConfig(issue);

		map.put("options", optionsManager.getOptions(fieldConfig));
		return map;
	}

	private OptionsManager getOptionsManager() {
		return ComponentAccessor.getComponent(OptionsManager.class);
	}

	@Override
	public Option getValueFromIssue(final CustomField field, final Issue issue) {
		final Option option = super.getValueFromIssue(field, issue);
		if (option == null) {
			return new Option() {
				@Override
				public Long getOptionId() {
					return PHANTOM_OPTION;
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

	@Override
	public int compare(@Nonnull String val1, @Nonnull String val2, FieldConfig fieldConfig) {
		Options options = this.getOptions(fieldConfig, null);
		if(options != null) {
			int v1 = options.indexOf(options.getOptionById(Long.valueOf(val1)));
			int v2 = options.indexOf(options.getOptionById(Long.valueOf(val2)));
			return v1 > v2?1:(v1 < v2?-1:0);
		} else {
			log.info("No options were found.");
			return 0;
		}
	}
}
