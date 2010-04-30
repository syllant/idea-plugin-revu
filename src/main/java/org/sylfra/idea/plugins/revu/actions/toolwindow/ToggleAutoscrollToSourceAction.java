package org.sylfra.idea.plugins.revu.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ToggleAutoscrollToSourceAction extends ToggleAction
{
  public boolean isSelected(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return false;
    }

    RevuWorkspaceSettings revuWorkspaceSettings = RevuUtils.getWorkspaceSettings(project);
    return revuWorkspaceSettings.isAutoScrollToSource();
  }

  public void setSelected(AnActionEvent e, boolean state)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    RevuWorkspaceSettingsComponent settingsComponent = project.getComponent(RevuWorkspaceSettingsComponent.class);
    RevuWorkspaceSettings settings = settingsComponent.getState();

    settings.setAutoScrollToSource(!settings.isAutoScrollToSource());
    settingsComponent.loadState(settings);
  }
}
