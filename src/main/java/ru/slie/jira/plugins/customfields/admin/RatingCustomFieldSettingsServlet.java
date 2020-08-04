package ru.slie.jira.plugins.customfields.admin;

import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;
import com.atlassian.templaterenderer.TemplateRenderer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RatingCustomFieldSettingsServlet extends HttpServlet {
    private final I18nHelper i18n;
    private final TemplateRenderer renderer;
    private final JiraBaseUrls jiraBaseUrls;
    private final WebSudoManager webSudoManager;
    private final UserManager userManager;
    private final RatingCustomFieldSettings ratingCustomFieldSettings;

    public RatingCustomFieldSettingsServlet(@ComponentImport I18nHelper i18n,
                                            @ComponentImport TemplateRenderer renderer,
                                            @ComponentImport JiraBaseUrls jiraBaseUrls,
                                            @ComponentImport WebSudoManager webSudoManager,
                                            @ComponentImport UserManager userManager,
                                            RatingCustomFieldSettings ratingCustomFieldSettings) {
        this.i18n = i18n;
        this.renderer = renderer;
        this.jiraBaseUrls = jiraBaseUrls;
        this.webSudoManager = webSudoManager;
        this.userManager = userManager;
        this.ratingCustomFieldSettings = ratingCustomFieldSettings;
    }

    protected void redirect(HttpServletResponse response, String url) throws IOException {
        response.sendRedirect(jiraBaseUrls.baseUrl() + url);
    }

    private void checkPermissions(HttpServletRequest request) throws WebSudoSessionException {
        webSudoManager.willExecuteWebSudoRequest(request);
        // If websudo disabled
        UserProfile user = userManager.getRemoteUser(request);
        if (user == null || !userManager.isSystemAdmin(user.getUserKey())) {
            throw new WebSudoSessionException();
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            checkPermissions(request);
            Map<String, Object> params = new HashMap<>();
            params.put("showEmptyField", ratingCustomFieldSettings.isShowEmptyField());
            params.put("baseUrl", jiraBaseUrls.baseUrl());
            renderer.render("/templates/customfield/rating/admin/settings.vm", params, response.getWriter());

        } catch(WebSudoSessionException wes) {
            webSudoManager.enforceWebSudoProtection(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            checkPermissions(request);
            Map<String, Object> params = new HashMap<>();

            String showEmptyField = request.getParameter("showEmptyField");
            ratingCustomFieldSettings.setShowEmptyField("yes".equals(showEmptyField));

            params.put("saved", true);
            params.put("showEmptyField", ratingCustomFieldSettings.isShowEmptyField());
            params.put("baseUrl", jiraBaseUrls.baseUrl());
            renderer.render("/templates/customfield/rating/admin/settings.vm", params, response.getWriter());

        } catch (WebSudoSessionException wes) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
