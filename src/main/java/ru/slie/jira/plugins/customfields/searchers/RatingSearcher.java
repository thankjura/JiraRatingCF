package ru.slie.jira.plugins.customfields.searchers;

import com.atlassian.jira.issue.customfields.searchers.SelectSearcher;
import com.atlassian.jira.util.JiraComponentFactory;
import com.atlassian.jira.util.JiraComponentLocator;

/**
 * Created with IntelliJ IDEA.
 * User: jura
 * Date: 23.07.13
 * Time: 15:31
 * To change this template use File | Settings | File Templates.
 */
public class RatingSearcher extends SelectSearcher {
    // TODO: implements > and < operators
    public RatingSearcher() {
        super(JiraComponentFactory.getInstance(), new JiraComponentLocator());
    }
}
