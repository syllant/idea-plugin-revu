package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.forms.ReviewBrowsingForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractSplitReviewBrowsingFormAction extends ToggleAction
{
  public void setSelected(AnActionEvent e, boolean state)
  {
    Project project = e.getData(DataKeys.PROJECT);
    ReviewBrowsingForm reviewBrowsingForm = retrieveReviewBrowsingForm(e);

    if (reviewBrowsingForm != null)
    {
      int orientation = getOrientation();
      reviewBrowsingForm.getSplitPane().setOrientation(orientation);
      reviewBrowsingForm.getSplitPane().setDividerLocation(0.5d);

      RevuWorkspaceSettingsComponent workspaceSettingsComponent =
        project.getComponent(RevuWorkspaceSettingsComponent.class);
      RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
      workspaceSettings.setToolWindowSplitOrientation(String.valueOf(orientation));
      workspaceSettingsComponent.loadState(workspaceSettings);
    }
  }

  public boolean isSelected(AnActionEvent e)
  {
    ReviewBrowsingForm reviewBrowsingForm = retrieveReviewBrowsingForm(e);

    return ((reviewBrowsingForm != null) && (reviewBrowsingForm.getSplitPane().getOrientation() == getOrientation()));
  }

  private ReviewBrowsingForm retrieveReviewBrowsingForm(AnActionEvent e)
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
