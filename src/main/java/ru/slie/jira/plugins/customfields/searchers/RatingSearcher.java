package ru.slie.jira.plugins.customfields.searchers;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SelectCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldContextValueGeneratingClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.searchers.transformer.SelectCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.customfields.statistics.SelectStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.jql.context.MultiClauseDecoratorContextFactory;
import com.atlassian.jira.jql.context.SelectCustomFieldClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.SelectCustomFieldClauseQueryFactory;
import com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory;
import com.atlassian.jira.jql.resolver.CustomFieldOptionResolver;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.jql.validator.SelectCustomFieldValidator;
import com.atlassian.jira.jql.values.CustomFieldOptionsClauseValuesGenerator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.web.FieldVisibilityManager;
import ru.slie.jira.plugins.customfields.indexer.RatingCustomFieldIndexer;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created with IntelliJ IDEA.
 * User: jura
 * Date: 23.07.13
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
public class RatingSearcher extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher, SortableCustomFieldSearcher, CustomFieldStattable {
    // TODO: implements > and < operators
    private volatile CustomFieldSearcherInformation searcherInformation;
    private volatile SearchInputTransformer searchInputTransformer;
    private volatile SearchRenderer searchRenderer;
    private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;
    private volatile ClauseNames clauseNames;
    private final ComponentFactory componentFactory;
    private CustomFieldInputHelper customFieldInputHelper;

    public RatingSearcher() {
        this.componentFactory = ComponentAccessor.getComponent(ComponentFactory.class);
    }

    public void init(CustomField field) {
        this.clauseNames = field.getClauseNames();
        FieldVisibilityManager fieldVisibilityManager = ComponentAccessor.getComponent(FieldVisibilityManager.class);
        JqlOperandResolver jqlOperandResolver = ComponentAccessor.getComponent(JqlOperandResolver.class);
        JqlSelectOptionsUtil jqlSelectOptionsUtil = ComponentAccessor.getComponent(JqlSelectOptionsUtil.class);
        QueryContextConverter queryContextConverter = ComponentAccessor.getComponent(QueryContextConverter.class);
        this.customFieldInputHelper = ComponentAccessor.getComponent(CustomFieldInputHelper.class);
        MultiClauseDecoratorContextFactory.Factory multiFactory = ComponentAccessor.getComponent(MultiClauseDecoratorContextFactory.Factory.class);
        FieldIndexer indexer = new RatingCustomFieldIndexer(fieldVisibilityManager, field);
        OperatorUsageValidator usageValidator = ComponentAccessor.getComponent(OperatorUsageValidator.class);
        CustomFieldOptionResolver customFieldOptionResolver = ComponentAccessor.getComponent(CustomFieldOptionResolver.class);
        CustomFieldValueProvider customFieldValueProvider = new SelectCustomFieldValueProvider();
        this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.singletonList(indexer), new AtomicReference<>(field));
        this.searchRenderer = new CustomFieldRenderer(this.clauseNames, this.getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
        this.searchInputTransformer = new SelectCustomFieldSearchInputTransformer(field, this.clauseNames, this.searcherInformation.getId(), jqlSelectOptionsUtil, queryContextConverter, jqlOperandResolver, this.customFieldInputHelper);
        ClauseQueryFactory queryFactory = new SelectCustomFieldClauseQueryFactory(field, jqlSelectOptionsUtil, jqlOperandResolver, customFieldOptionResolver);
        queryFactory = new ValidatingDecoratorQueryFactory(usageValidator, queryFactory);
        this.customFieldSearcherClauseHandler = new SimpleCustomFieldContextValueGeneratingClauseHandler(this.componentFactory.createObject(SelectCustomFieldValidator.class, field), queryFactory, multiFactory.create(this.componentFactory.createObject(SelectCustomFieldClauseContextFactory.class, field), false), ComponentAccessor.getComponent(CustomFieldOptionsClauseValuesGenerator.class), OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.OPTION);
    }

    public SearcherInformation<CustomField> getSearchInformation() {
        if (this.searcherInformation == null) {
            throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
        } else {
            return this.searcherInformation;
        }
    }

    public SearchInputTransformer getSearchInputTransformer() {
        if (this.searchInputTransformer == null) {
            throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
        } else {
            return this.searchInputTransformer;
        }
    }

    public SearchRenderer getSearchRenderer() {
        if (this.searchRenderer == null) {
            throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
        } else {
            return this.searchRenderer;
        }
    }

    public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
        if (this.customFieldSearcherClauseHandler == null) {
            throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
        } else {
            return this.customFieldSearcherClauseHandler;
        }
    }

    public StatisticsMapper<?> getStatisticsMapper(CustomField customField) {
        if (this.clauseNames == null) {
            throw new IllegalStateException("Attempt to retrieve Statistics Mapper off uninitialised custom field searcher.");
        } else {
            SelectConverter selectConverter = ComponentAccessor.getComponent(SelectConverter.class);
            return new SelectStatisticsMapper(customField, selectConverter, ComponentAccessor.getJiraAuthenticationContext(), this.customFieldInputHelper);
        }
    }

    @Nonnull
    public LuceneFieldSorter<?> getSorter(CustomField customField) {
        return this.getStatisticsMapper(customField);
    }
}
