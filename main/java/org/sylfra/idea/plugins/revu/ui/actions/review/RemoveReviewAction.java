package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.model.Review;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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
      ? "settings.project.confirmRemoveReview.text"
      : "settings.project.confirmRemoveReviewWithAfferentLink.text";
    int result = Messages.showOkCancelDialog(liReviews,
      RevuBundle.message(msgKey, selectedReview.getName()),
      RevuBundle.message("settings.project.confirmRemoveReview.title"),
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

  protected boolean isEnabledOnlyForNonEmbedded()
  {
    return true;
  }
}
