package ru.slie.jira.plugins.customfields.indexer;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.SecurityIndexingUtils;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class RatingCustomFieldIndexer extends AbstractCustomFieldIndexer {
    private static final Logger log = LoggerFactory.getLogger(RatingCustomFieldIndexer.class);

    private final CustomField customField;

    public RatingCustomFieldIndexer(FieldVisibilityManager fieldVisibilityManager, CustomField customField) {
        super(fieldVisibilityManager, Assertions.notNull("customField", customField));
        this.customField = customField;
    }

    public void addDocumentFieldsSearchable(Document doc, Issue issue) {
        this.addDocumentFields(doc, issue, true);
    }

    public void addDocumentFieldsNotSearchable(Document doc, Issue issue) {
        this.addDocumentFields(doc, issue, false);
    }

    private void addDocumentFields(Document doc, Issue issue, boolean searchable) {
        try {
            Object value = this.customField.getValue(issue);
            if (value instanceof Collection) {
                for (Object o: (Collection<?>)value) {
                    this.addFields(doc, issue, searchable, (Option) o);
                }
            } else if (value instanceof Option) {
                this.addFields(doc, issue, searchable, (Option)value);
            }
        } catch (NumberFormatException var5) {
            log.warn("Invalid custom field option");
        }

    }

    private void addFields(Document doc, Issue issue, boolean searchable, Option value) {
        if (value == null || value.getOptionId() == null || value.getOptionId() == -1) {
            return;
        }
        String indexValue = value.getOptionId().toString();
        if (searchable) {
            doc.add(new StringField(this.getDocumentFieldId(), indexValue, Field.Store.YES));
            doc.add(new StringField(this.getDocumentFieldId() + "_raw", indexValue, Field.Store.YES));
            doc.add(new SortedSetDocValuesField(this.getDocumentFieldId(), new BytesRef(indexValue)));
            doc.add(new SortedSetDocValuesField(this.getDocumentFieldId() + "_raw", new BytesRef(indexValue)));
        } else {
            doc.add(new StoredField(this.getDocumentFieldId(), indexValue));
            doc.add(new StoredField(this.getDocumentFieldId() + "_raw", indexValue));
        }

        SecurityIndexingUtils.indexPermissions(doc, issue, this.getDocumentFieldId(), indexValue);
    }
}
