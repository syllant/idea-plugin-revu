package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.RevuUtils;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemDialog;

import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateReviewItemAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Editor editor = e.getData(DataKeys.EDITOR);
    VirtualFile virtualFile = e.getData(DataKeys.VIRTUAL_FILE);

    ReviewItem item = new ReviewItem();
    item.setFile(virtualFile);
    if (editor != null)
    {
      item.setLineStart(editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionStart()) + 1);
      item.setLineEnd(editor.getDocument().getLineNumber(editor.getSelectionModel().getSelectionEnd()) + 1);
    }
    item.setResolutionStatus(ItemResolutionStatus.TO_RESOLVE);

    ReviewItemDialog dialog = new ReviewItemDialog(project);
    dialog.show(item, true);
    if (dialog.getExitCode() == DialogWrapper.OK_EXIT_CODE)
    {
      dialog.updateData(item);

      Review review = item.getReview();

      User user = review.getReviewReferential().getUser(RevuUtils.getCurrentUserLogin());
      assert (user != null) : "User should be declared in review";

      History history = new History();
      Date now = new Date();
      history.setCreatedBy(user);
      history.setCreatedOn(now);
      history.setLastUpdatedBy(user);
      history.setLastUpdatedOn(now);
      item.setHistory(history);

      review.addItem(item);
    }
  }

}
