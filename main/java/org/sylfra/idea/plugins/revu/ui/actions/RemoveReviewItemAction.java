package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RemoveReviewItemAction extends AbstractReviewItemAction
{
  @Override
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    ReviewItem item = e.getData(RevuDataKeys.REVIEW_ITEM);

    if (item != null)
    {
      Review review = item.getReview();
      review.removeItem(item);

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.save(review);
    }
  }
}