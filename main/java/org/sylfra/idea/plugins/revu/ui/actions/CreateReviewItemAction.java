package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.history.Clock;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.*;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemDialog;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

import java.util.Date;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateReviewItemAction extends AbstractReviewItemAction
{
  @Override
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Editor editor = e.getData(DataKeys.EDITOR);
    VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);

    if ((project == null) || (vFile == null))
    {
      return;
    }

    ReviewItem item = new ReviewItem();
    item.setFile(vFile);
    if (editor != null)
    {
      Document document = editor.getDocument();
      int lineStart = document.getLineNumber(editor.getSelectionModel().getSelectionStart());
      int lineEnd = document.getLineNumber(editor.getSelectionModel().getSelectionEnd());

      item.setLineStart(lineStart);
      item.setLineEnd(lineEnd);
      CharSequence fragment = document.getCharsSequence().subSequence(document.getLineStartOffset(lineStart),
        document.getLineEndOffset(lineEnd));
      item.setHash(fragment.toString().hashCode());
    }
    item.setResolutionStatus(ItemResolutionStatus.TO_RESOLVE);

    ReviewItemDialog dialog = new ReviewItemDialog(project);
    dialog.show(item, true);
    if (dialog.isOK())
    {
      dialog.updateData(item);

      Review review = item.getReview();

      User user = review.getDataReferential().getUser(RevuUtils.getCurrentUserLogin(), true);
      assert (user != null) : "User should be declared in review";

      History history = new History();
      Date now = new Date();
      history.setCreatedBy(user);
      history.setCreatedOn(now);
      history.setLastUpdatedBy(user);
      history.setLastUpdatedOn(now);
      item.setHistory(history);

      // VCS revision number will be saved at commit
      item.setLocalRev(String.valueOf(Clock.getCurrentTimestamp()));
      VcsRevisionNumber vcsRev = RevuVcsUtils.getVcsRevisionNumber(project, vFile);
      if (vcsRev != null)
      {
        item.setVcsRev(vcsRev.toString());
      }

      review.addItem(item);

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.save(review);
    }
  }

}
