package org.sylfra.idea.plugins.revu.ui.actions.reviewitem;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    List<ReviewItem> items = e.getData(RevuDataKeys.REVIEW_ITEM_ARRAY);

    Set<Review> reviewsToSave = new HashSet<Review>();
    if (items != null)
    {
      for (ReviewItem item : items)
      {
        Review review = item.getReview();
        review.removeItem(item);

        reviewsToSave.add(review);
      }

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewsToSave)
      {
        reviewManager.save(review);
      }
    }
  }
}