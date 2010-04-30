package org.sylfra.idea.plugins.revu.actions.reviewing;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.DiffActionExecutor;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.diff.ItemLatestState;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.impl.VcsBackgroundableActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class CompareWithAction extends AnAction
{
  private FileScopeManager fileScopeManager;

  public CompareWithAction()
  {
    fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);
  }

  protected AbstractVcs getVcs(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);
    if ((project == null) || (vFile == null))
    {
      return null;
    }

    return VcsUtil.getVcsFor(project, vFile);
  }

  @Override
  public void actionPerformed(AnActionEvent e)
  {
    AbstractVcs vcs = getVcs(e);

    Project project = e.getData(DataKeys.PROJECT);
    VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);

    final FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(vFile);
    if ((filePath == null) || (!vcs.fileIsUnderVcs(filePath)))
    {
      return;
    }

    Review review = RevuUtils.getReviewingReview(project);

    VcsRevisionNumber revision;
    if (review.getFileScope().getVcsAfterRev() != null)
    {
      revision = vcs.parseRevisionNumber(review.getFileScope().getVcsAfterRev());
    }
    else
    {
      ItemLatestState itemLatestState = vcs.getDiffProvider().getLastRevision(vFile);
      if (itemLatestState == null)
      {
        return;
      }

      revision = itemLatestState.getNumber();
    }
    
    DiffActionExecutor.showDiff(vcs.getDiffProvider(), revision, vFile, project,
      VcsBackgroundableActions.COMPARE_WITH);
  }

  @Override
  public void update(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    Review review = RevuUtils.getReviewingReview(project);
    if (review == null)
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.comparewith.template.text"));
      e.getPresentation().setEnabled(false);
    }
    else
    {
      e.getPresentation().setText(RevuBundle.message("reviewing.comparewith.review.text", review.getName()));
      VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);
      e.getPresentation().setEnabled((vFile != null)
        && RevuVcsUtils.isUnderVcs(project, vFile)
        && fileScopeManager.belongsToScope(project, review, vFile));
    }
  }
}