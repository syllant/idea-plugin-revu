package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.ide.projectView.ProjectView;
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
public class ToggleFilterIssuesAction extends ToggleAction
{
  public void setSelected(AnActionEvent e, boolean state)
  {
    Project project = e.getData(DataKeys.PROJECT);

    if (project != null)
    {
      RevuWorkspaceSettingsComponent workspaceSettingsComponent =
        project.getComponent(RevuWorkspaceSettingsComponent.class);
      RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
      workspaceSettings.setFilterFilesWithIssues(!workspaceSettings.isFilterFilesWithIssues());
      workspaceSettingsComponent.loadState(workspaceSettings);

      ProjectView.getInstance(project).getCurrentProjectViewPane().updateFromRoot(false);
    }
  }

  public boolean isSelected(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);

    return (project != null) && RevuUtils.getWorkspaceSettings(project).isFilterFilesWithIssues();
  }

}