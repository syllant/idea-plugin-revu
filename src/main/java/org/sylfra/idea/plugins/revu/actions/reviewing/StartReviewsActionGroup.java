package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewStatus;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.projectView.RevuProjectViewPane;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

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

    Review reviewingReview = RevuUtils.getReviewingReview(project);
    if (reviewingReview != null)
    {
      e.getPresentation().setEnabled(false);
      return;
    }

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    Collection<Review> reviews = reviewManager.getReviews(RevuUtils.getCurrentUserLogin(), REVIEWABLE_STATUSES);

    SortedSet<StartReviewAction> actions = new TreeSet<StartReviewAction>();
    for (Review review : reviews)
    {
      if (RevuUtils.hasRole(review, User.Role.REVIEWER))
      {
        actions.add(new StartReviewAction(review));
      }
    }

    if (actions.isEmpty())
    {
      e.getPresentation().setEnabled(false);
    }
    else
    {
      e.getPresentation().setEnabled(true);
      removeAll();
      addAll(actions.toArray(new AnAction[actions.size()]));
    }

  }

  private class StartReviewAction extends AnAction implements Comparable<StartReviewAction>
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

      review.setStatus(ReviewStatus.REVIEWING);
      project.getComponent(ReviewManager.class).saveChanges(review);

      ProjectViewSelectInTarget.select(project, this, RevuProjectViewPane.ID, review.getName(), null, true);

      RevuWorkspaceSettingsComponent workspaceSettingsComponent =
        project.getComponent(RevuWorkspaceSettingsComponent.class);
      RevuWorkspaceSettings workspaceSettings = workspaceSettingsComponent.getState();
      workspaceSettings.setReviewingReviewName(review.getName());
      workspaceSettingsComponent.loadState(workspaceSettings);
    }

    public int compareTo(StartReviewAction o)
    {
      return review.compareTo(o.review);
    }
  }
}
