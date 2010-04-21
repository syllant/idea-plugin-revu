package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StopReviewAction extends AnAction
{
  @Override
  public void update(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    Review reviewingReview = RevuUtils.getReviewingReview(project);
    if (reviewingReview != null)
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.stopReviewing.review.text", reviewingReview.getName()));
      e.getPresentation().setVisible(true);
    }
    else
    {
      e.getPresentation().setVisible(false);
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

    review.setStatus(ReviewStatus.FIXING);
    project.getComponent(ReviewManager.class).saveChanges(review);

    RevuWorkspaceSettingsComponent workspaceSettingsComponent =
      project.getComponent(RevuWorkspaceSettingsComponent.class);
    RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
    workspaceSettings.setReviewingReviewName(null);
    workspaceSettingsComponent.loadState(workspaceSettings);
  }
}