package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.forms.settings.RevuProjectSettingsForm;
import org.sylfra.idea.plugins.revu.utils.ReviewFileChooser;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

import java.io.File;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
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

    VirtualFile vFile = fileChooser.selectFileToOpen(
      RevuVfsUtils.findFile(RevuUtils.getWorkspaceSettings(project).getLastSelectedReviewDir()));
    if (vFile != null)
    {
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      Review review = reviewManager.getReviewByFile(new File(vFile.getPath()));
      if (review != null)
      {
        Messages.showWarningDialog(project,
          RevuBundle.message("projectSettings.review.import.fileAlreadyExists.text", review.getName()),
          RevuBundle.message("projectSettings.review.import.error.title"));
        return;
      }

      //@TODO check path outside from project
      review = new Review();
      review.setFile(new File(vFile.getPath()));

      if (!reviewManager.load(review, false))
      {
        return;
      }

      if (reviewManager.getReviewByName(review.getName()) != null)
      {
        Messages.showWarningDialog(project,
          RevuBundle.message("projectSettings.review.import.nameAlreadyExists.text", review.getName()),
          RevuBundle.message("projectSettings.review.import.error.title"));
        return;
      }

      final RevuProjectSettingsForm form = project.getComponent(RevuProjectSettingsForm.class);
      form.addItem(review);
    }
  }
}
