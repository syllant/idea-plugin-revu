package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RemoveReviewItemAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    ReviewItem item = e.getData(RevuDataKeys.REVIEW_ITEM);

    if (item != null)
    {
      item.getReview().removeItem(item);
    }
  }
}