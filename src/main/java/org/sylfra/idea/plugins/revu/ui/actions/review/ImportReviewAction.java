package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;

import javax.swing.*;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class ImportReviewAction extends AbstractReviewSettingsAction
{
  private ReviewFileChooser fileChooser;

  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    if (fileChooser == null)
    {
      fileChooser = new ReviewFileChooser(project);
    }

    VirtualFile vFile = fileChooser.selectFileToOpen(null);
    if (vFile != null)
    {
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      Review review = reviewManager.getReviewByPath(vFile.getPath());
      if (review != null)
      {
        Messages.showWarningDialog(project,
          RevuBundle.message("projectSettings.review.import.fileAlreadyExists.text", review.getName()),
          RevuBundle.message("projectSettings.review.import.error.title"));
        return;
      }

      //@TODO check path outside from project
      JList liReviews = (JList) e.getData(DataKeys.CONTEXT_COMPONENT);
      review = new Review();
      review.setPath(vFile.getPath());

      reviewManager.load(review, true);

      if (reviewManager.getReviewByName(review.getName()) != null)
      {
        Messages.showWarningDialog(project,
          RevuBundle.message("projectSettings.review.import.nameAlreadyExists.text", review.getName()),
          RevuBundle.message("projectSettings.review.import.error.title"));
        return;
      }

      DefaultListModel model = (DefaultListModel) liReviews.getModel();
      model.addElement(review);
      liReviews.setSelectedValue(review, true);
    }
  }
}
