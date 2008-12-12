package org.sylfra.idea.plugins.revu.ui.actions.reviewitem;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemDialog;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowUpdateReviewItemPopupAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    ReviewItem item = e.getData(RevuDataKeys.REVIEW_ITEM);

    if (item == null)
    {
      return;
    }

    ReviewItemDialog dialog = new ReviewItemDialog(project);
    dialog.show(item, false);
    if (dialog.isOK())
    {
      dialog.updateData(item);
    }
  }
}