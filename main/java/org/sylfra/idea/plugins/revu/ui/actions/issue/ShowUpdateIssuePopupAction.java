package org.sylfra.idea.plugins.revu.ui.actions.issue;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.ui.forms.issue.IssueDialog;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowUpdateIssuePopupAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Issue issue = e.getData(RevuDataKeys.ISSUE);

    if (issue == null)
    {
      return;
    }

    IssueDialog dialog = new IssueDialog(project);
    dialog.show(issue, false);
    if (dialog.isOK())
    {
      dialog.updateData(issue);
    }
  }
}