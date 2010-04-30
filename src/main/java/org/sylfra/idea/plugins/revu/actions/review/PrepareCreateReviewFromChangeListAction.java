package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.changes.ChangeList;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: InsidePrepareCreateReviewAction.java 22 2010-04-03 17:16:12Z syllant $
 */
public class PrepareCreateReviewFromChangeListAction extends PrepareCreateReviewAction
{
  @Override
  protected DefaultActionGroup createActionGroup(AnActionEvent e)
  {
    ChangeList[] changeLists = e.getData(VcsDataKeys.CHANGE_LISTS);
    CommittedChangeList firstChangeList = ((changeLists == null)
      || (changeLists.length == 0) || (!(changeLists[0] instanceof CommittedChangeList))) ? null :
      (CommittedChangeList) changeLists[0];

    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new CreateReviewAction(false, firstChangeList));
    actionGroup.add(new CreateReviewAction(true, firstChangeList));

    return actionGroup;
  }

  public void update(final AnActionEvent e)
  {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    final ChangeList[] changeLists = e.getData(VcsDataKeys.CHANGE_LISTS);
    e.getPresentation().setEnabled(project != null && changeLists != null && changeLists.length == 1 &&
      changeLists[0] instanceof CommittedChangeList);
  }
}