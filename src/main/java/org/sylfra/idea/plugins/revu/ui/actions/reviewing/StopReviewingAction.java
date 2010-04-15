package org.sylfra.idea.plugins.revu.ui.actions.reviewing;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StopReviewingAction extends AnAction
{
  @Override
  public void update(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    String reviewName = RevuUtils.getWorkspaceSettings(project).getReviewingReviewName();
    if (reviewName == null)
    {
      e.getPresentation().setVisible(false);
    }
    else
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.stopReviewing.review.text", reviewName));
      e.getPresentation().setVisible(true);
    }
  }

  @Override
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);

    Review review = RevuUtils.getReviewingReview(project);
    if (review == null)
    {
      return;
    }

    RevuWorkspaceSettingsComponent workspaceSettingsComponent =
      project.getComponent(RevuWorkspaceSettingsComponent.class);
    RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
    workspaceSettings.setReviewingReviewName(null);
    workspaceSettingsComponent.loadState(workspaceSettings);
  }
}