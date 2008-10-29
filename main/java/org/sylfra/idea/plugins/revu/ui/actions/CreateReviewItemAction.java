package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.ComponentProvider;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateReviewItemAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Editor editor = e.getData(DataKeys.EDITOR);
    VirtualFile virtualFile = e.getData(DataKeys.VIRTUAL_FILE);
    ReviewManager reviewManager = ComponentProvider.getReviewManager(project);

    Review review = reviewManager.getActiveReview();
    if (review == null)
    {
      review = new Review();
      review.setActive(true);
      review.setTitle("test");
      reviewManager.addReview(review);
    }

    ReviewItem item = new ReviewItem();
    item.setTitle("[title] test-" + editor.getSelectionModel().getSelectionStart());
    item.setDesc("[desc] test\ntest-" + editor.getSelectionModel().getSelectionStart());
    item.setFilePath(virtualFile.getPath());
    item.setLineStart(
      editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()));
    item
      .setLineEnd(editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionEnd()));

    review.addItem(item);
  }
}
