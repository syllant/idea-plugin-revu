package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.ui.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.forms.ReviewBrowsingForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class SaveReviewAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    ReviewBrowsingForm browsingForm = project.getComponent(RevuToolWindowManager.class).getSelectedReviewBrowsingForm();
    browsingForm.saveIfModified();
  }
}