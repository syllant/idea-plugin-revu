package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.ui.RevuToolWindowManager;
import org.sylfra.idea.plugins.revu.ui.forms.ReviewBrowsingForm;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class AbstractSplitReviewBrowsingFormAction extends ToggleAction
{
  public void setSelected(AnActionEvent e, boolean state)
  {
    ReviewBrowsingForm reviewBrowsingForm = retrieveReviewBrowsingForm(e);

    if (reviewBrowsingForm != null)
    {
      reviewBrowsingForm.getSplitPane().setOrientation(getOrientation());
    }
  }

  public boolean isSelected(AnActionEvent e)
  {
    ReviewBrowsingForm reviewBrowsingForm = retrieveReviewBrowsingForm(e);

    return ((reviewBrowsingForm != null) && (reviewBrowsingForm.getSplitPane().getOrientation() == getOrientation()));
  }

  private ReviewBrowsingForm retrieveReviewBrowsingForm(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return null;
    }

    return ServiceManager.getService(project, RevuToolWindowManager.class).getSelectedReviewBrowsingForm();
  }

  protected abstract int getOrientation();
}
