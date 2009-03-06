package org.sylfra.idea.plugins.revu.ui.forms.filter;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.model.Filter;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.forms.AbstractListUpdatableForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.util.List;

/**
 * Used to interface settings inside Settings panel
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FilterSettingsForm extends AbstractListUpdatableForm<Filter, FilterForm>
{
  public FilterSettingsForm(@NotNull Project project)
  {
    super(project);
  }

  @Nls
  public String getDisplayName()
  {
    return RevuBundle.message("filterSettings.title");
  }

  public Icon getIcon()
  {
    return RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU_LARGE);
  }

  public String getHelpTopic()
  {
    return null;
  }

  public JComponent createComponent()
  {
    return contentPane;
  }

  public void disposeUIResources()
  {
    dispose();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void apply(List<Filter> items) throws ConfigurationException
  {
    RevuWorkspaceSettingsComponent workspaceSettingsComponent =
      project.getComponent(RevuWorkspaceSettingsComponent.class);

    RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
    workspaceSettings.setFilters(items);
    workspaceSettingsComponent.loadState(workspaceSettings);
  }

  protected FilterForm createMainForm()
  {
    return new FilterForm(project);
  }

  protected ActionGroup createActionGroup()
  {
    return (ActionGroup) ActionManager.getInstance().getAction("revu.settings.project.filters");
  }

  @NotNull
  protected List<Filter> getOriginalItems()
  {
    return RevuUtils.getWorkspaceSettings(project).getFilters();
  }

  @Override
  @Nls
  protected String getMessageKeyWhenNoSelection()
  {
    return "filterSettings.noSelection.text";
  }
}