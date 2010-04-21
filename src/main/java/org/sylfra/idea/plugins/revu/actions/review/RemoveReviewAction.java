package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class RemoveReviewAction extends AbstractReviewSettingsAction
{
  public void actionPerformed(AnActionEvent e)
  {
    JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
    DefaultListModel model = (DefaultListModel) liReviews.getModel();
    Review selectedReview = (Review) liReviews.getSelectedValue();

    // Check afferent link
    List<Review> afferentReviews = new ArrayList<Review>();
    for (int i=0; i<model.getSize(); i++)
    {
      Review review = (Review) model.get(i);
      if (selectedReview.equals(review.getExtendedReview()))
      {
        afferentReviews.add(review);
      }
    }

    String msgKey = afferentReviews.isEmpty()
      ? "projectSettings.confirmRemoveReview.text"
      : "projectSettings.confirmRemoveReviewWithAfferentLink.text";
    int result = Messages.showOkCancelDialog(liReviews,
      RevuBundle.message(msgKey, selectedReview.getName()),
      RevuBundle.message("projectSettings.confirmRemoveReview.title"),
      Messages.getWarningIcon());

    if (result == DialogWrapper.OK_EXIT_CODE)
    {
      model.removeElement(selectedReview);
      for (Review review : afferentReviews)
      {
        review.setExtendedReview(null);
      }
      liReviews.setSelectedIndex(0);
    }
  }

  protected boolean isEnabledForReview(@NotNull Review review)
  {
    User user = RevuUtils.getCurrentUser(review);
    Set<User> adminUsers = review.getDataReferential().getUsersByRole(true).get(User.Role.ADMIN);
    return ((!review.isEmbedded())
      && (((adminUsers == null) || (adminUsers.isEmpty())) || ((user != null) && (user.hasRole(User.Role.ADMIN)))));
  }
}
