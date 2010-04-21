package org.sylfra.idea.plugins.revu.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.IssueBrowsingPane;
import org.sylfra.idea.plugins.revu.ui.RevuToolWindowManager;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractSplitReviewBrowsingFormAction extends ToggleAction
{
  public void setSelected(AnActionEvent e, boolean state)
  {
    Project project = e.getData(DataKeys.PROJECT);
    IssueBrowsingPane issueBrowsingPane = retrieveReviewBrowsingForm(e);

    if (issueBrowsingPane != null)
    {
      int orientation = getOrientation();
      issueBrowsingPane.getSplitPane().setOrientation(orientation);

      RevuWorkspaceSettingsComponent workspaceSettingsComponent =
        project.getComponent(RevuWorkspaceSettingsComponent.class);
      RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
      workspaceSettings.setToolWindowSplitOrientation(String.valueOf(orientation));
      workspaceSettingsComponent.loadState(workspaceSettings);
    }
  }

  public boolean isSelected(AnActionEvent e)
  {
    IssueBrowsingPane issueBrowsingPane = retrieveReviewBrowsingForm(e);

    return ((issueBrowsingPane != null) && (issueBrowsingPane.getSplitPane().getOrientation() == getOrientation()));
  }

  private IssueBrowsingPane retrieveReviewBrowsingForm(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return null;
    }

    return project.getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm();
  }

  protected abstract int getOrientation();
}
