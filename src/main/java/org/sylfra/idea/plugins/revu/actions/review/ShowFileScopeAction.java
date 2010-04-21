package org.sylfra.idea.plugins.revu.actions.review;

import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.projectView.RevuProjectViewPane;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowFileScopeAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    Review review = e.getData(RevuDataKeys.REVIEW);

    if ((project == null) || (review == null))
    {
      return;
    }

    ProjectViewSelectInTarget.select(project, this, RevuProjectViewPane.ID, review.getName(), null, true);
  }
}