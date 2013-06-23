package org.sylfra.idea.plugins.revu.actions.issue;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RemoveIssueAction extends AbstractIssueAction
{
  @Override
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    List<Issue> issues = e.getData(RevuDataKeys.ISSUE_LIST);

    if (issues == null)
    {
      Issue issue = e.getData(RevuDataKeys.ISSUE);
      if (issue == null)
      {
        return;
      }

      issues = new ArrayList<Issue>();
      issues.add(issue);
    }

    Set<Review> reviewsToSave = new HashSet<Review>();
    for (Issue issue : issues)
    {
      Review review = issue.getReview();
      review.removeIssue(issue);

      reviewsToSave.add(review);
    }

    ReviewManager reviewManager = project.getComponent(ReviewManager.class);
    for (Review review : reviewsToSave)
    {
      reviewManager.saveSilently(review);
    }
  }

  @Override
  public void update(AnActionEvent e)
  {
    boolean enabled;

    Issue currentIssue = e.getData(RevuDataKeys.ISSUE);
    if (currentIssue == null)
    {
      enabled = false;
    }
    else
    {
      Review review = currentIssue.getReview();
      User user = RevuUtils.getCurrentUser(review);
      enabled = (user != null) &&
        (RevuUtils.isActive(review) && user.hasRole(User.Role.REVIEWER) || (user.hasRole(User.Role.ADMIN)));
    }

    e.getPresentation().setEnabled(enabled);
  }
}