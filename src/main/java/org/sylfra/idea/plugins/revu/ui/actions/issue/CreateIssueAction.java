package org.sylfra.idea.plugins.revu.ui.actions.issue;

import com.intellij.history.Clock;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.model.IssueStatus;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.User;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssueDialog;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CreateIssueAction extends AbstractIssueAction
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

    Issue issue = new Issue();
    issue.setFile(vFile);
    if (editor != null)
    {
      Document document = editor.getDocument();
      int lineStart = document.getLineNumber(editor.getSelectionModel().getSelectionStart());
      int lineEnd = document.getLineNumber(editor.getSelectionModel().getSelectionEnd());

      issue.setLineStart(lineStart);
      issue.setLineEnd(lineEnd);
      CharSequence fragment = document.getCharsSequence().subSequence(document.getLineStartOffset(lineStart),
        document.getLineEndOffset(lineEnd));
      issue.setHash(fragment.toString().hashCode());
    }
    issue.setStatus(IssueStatus.TO_RESOLVE);

    IssueDialog dialog = new IssueDialog(project, true);
    dialog.show(issue);
    if (dialog.isOK())
    {
      dialog.updateData(issue);

      Review review = issue.getReview();

      assert (RevuUtils.getCurrentUserLogin() != null) : "Login should be set";

      issue.setHistory(RevuUtils.buildHistory(review));

      if (RevuVcsUtils.fileIsModifiedFromVcs(project, vFile))
      {
        issue.setLocalRev(String.valueOf(Clock.getCurrentTimestamp()));
      }

      if (issue.getFile() != null)
      {
        VcsRevisionNumber vcsRev = RevuVcsUtils.getVcsRevisionNumber(project, issue.getFile());
        if (vcsRev != null)
        {
          issue.setVcsRev(vcsRev.toString());
        }
      }

      review.addIssue(issue);

      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.saveSilently(review);
    }
  }

  @Override
  public void update(AnActionEvent e)
  {
    boolean enabled = false;
    Project project = e.getData(DataKeys.PROJECT);
    VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);

    if ((project != null) && (vFile != null))
    {
      List<Review> reviews = project.getComponent(ReviewManager.class).getReviews();
      for (Review review : reviews)
      {
        User user = RevuUtils.getCurrentUser(review);
        if (user == null)
        {
          continue;
        }

        boolean mayReview = RevuUtils.isActive(review) && user.hasRole(User.Role.REVIEWER);
        if ((mayReview || (user.hasRole(User.Role.ADMIN))))
        {
          enabled = true;
          break;
        }
      }
    }

    e.getPresentation().setEnabled(enabled);
  }
}
