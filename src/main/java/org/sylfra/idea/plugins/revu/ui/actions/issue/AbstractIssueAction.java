package org.sylfra.idea.plugins.revu.ui.actions.issue;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.List;

abstract class AbstractIssueAction extends AnAction
{
  @Override
  public void update(AnActionEvent e)
  {
    boolean enabled = false;
    String login = RevuUtils.getCurrentUserLogin();

    if (login != null)
    {
      Project project = e.getData(DataKeys.PROJECT);

      if (project != null)
      {
        List<Review> reviews = project.getComponent(ReviewManager.class).getReviews(true, false);
        for (Review review : reviews)
        {
          User user = review.getDataReferential().getUser(login, true);
          if ((user != null) && ((user.hasRole(User.Role.REVIEWER)) || (user.hasRole(User.Role.ADMIN))))
          {
            enabled = true;
            break;
          }
        }
      }
    }

    e.getPresentation().setEnabled(enabled);
  }
}