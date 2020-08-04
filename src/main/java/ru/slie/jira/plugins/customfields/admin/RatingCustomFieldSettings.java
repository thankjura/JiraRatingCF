package ru.slie.jira.plugins.customfields.admin;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RatingCustomFieldSettings {
    private final PluginSettings pluginSettings;
    private static final String SHOW_EMPTY_FIELD = "show-empty";
    private static final String BOOL_YES = "yes";
    private static final String BOOL_NO = "no";

    @Autowired
    public RatingCustomFieldSettings(@ComponentImport PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createSettingsForKey("ru.slie.jira.customfield.rating");
    }

    public boolean isShowEmptyField() {
        String value = (String) pluginSettings.get(SHOW_EMPTY_FIELD);
        if (value != null) {
            return value.toLowerCase().equals(BOOL_YES);
        }

        return true;
    }

    public void setShowEmptyField(boolean value) {
        if (value) {
            pluginSettings.put(SHOW_EMPTY_FIELD, BOOL_YES);
        } else {
            pluginSettings.put(SHOW_EMPTY_FIELD, BOOL_NO);
        }
    }
}
