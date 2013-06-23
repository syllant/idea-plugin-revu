package org.sylfra.idea.plugins.revu.actions.issue;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssueDialog;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowUpdateIssuePopupAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(PlatformDataKeys.PROJECT);
    Issue issue = e.getData(RevuDataKeys.ISSUE);

    if ((issue == null) || (project == null))
    {
      return;
    }

    IssueDialog dialog = new IssueDialog(project, false);
    dialog.show(issue);
    if (dialog.isOK())
    {
      dialog.updateData(issue);
    }
  }
}