package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ToggleAutoscrollToSourceAction extends ToggleAction
{
  public boolean isSelected(AnActionEvent e)
  {
    RevuWorkspaceSettings revuWorkspaceSettings = retrieveSettings(e);
    return ((revuWorkspaceSettings != null) && (revuWorkspaceSettings.isAutoScrollToSource()));
  }

  public void setSelected(AnActionEvent e, boolean state)
  {
    RevuWorkspaceSettings revuWorkspaceSettings = retrieveSettings(e);

    revuWorkspaceSettings.setAutoScrollToSource(!revuWorkspaceSettings.isAutoScrollToSource());
  }

  private RevuWorkspaceSettings retrieveSettings(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return null;
    }

    RevuWorkspaceSettingsComponent projectSettingsComponent = project.getComponent(
      RevuWorkspaceSettingsComponent.class);

    return projectSettingsComponent.getState();
  }
}
