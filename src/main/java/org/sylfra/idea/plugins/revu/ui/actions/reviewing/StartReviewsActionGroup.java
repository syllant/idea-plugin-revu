package org.sylfra.idea.plugins.revu.ui.actions.reviewing;

import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.projectView.RevuProjectViewPane;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class StartReviewsActionGroup extends DefaultActionGroup
{
  private final static ReviewStatus[] REVIEWABLE_STATUSES
    = {ReviewStatus.DRAFT, ReviewStatus.FIXING, ReviewStatus.REVIEWING};

  @Override
  public void update(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    boolean enabled = RevuUtils.getWorkspaceSettings(project).getReviewingReviewName() == null;
    e.getPresentation().setVisible(enabled);

    if (enabled)
    {
      removeAll();

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewManager.getReviews(RevuUtils.getCurrentUserLogin(), REVIEWABLE_STATUSES))
      {
        add(new StartReviewAction(review));
      }
    }
  }

  private class StartReviewAction extends AnAction
  {
    private final Review review;

    public StartReviewAction(Review review)
    {
      super(review.getName());
      this.review = review;
    }

    @Override
    public void actionPerformed(AnActionEvent e)
    {
      Project project = e.getData(DataKeys.PROJECT);
      if (project == null)
      {
        return;
      }

      ProjectViewSelectInTarget.select(project, this, RevuProjectViewPane.ID, review.getName(), null, true);

      RevuWorkspaceSettingsComponent workspaceSettingsComponent =
        project.getComponent(RevuWorkspaceSettingsComponent.class);
      RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
      workspaceSettings.setReviewingReviewName(review.getName());
      workspaceSettingsComponent.loadState(workspaceSettings);
    }
  }
}
