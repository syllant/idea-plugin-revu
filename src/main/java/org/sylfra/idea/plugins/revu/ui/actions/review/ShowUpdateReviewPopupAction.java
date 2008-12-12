package org.sylfra.idea.plugins.revu.ui.actions.review;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.forms.settings.project.RevuProjectSettingsForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowUpdateReviewPopupAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    final Review review = e.getData(RevuDataKeys.REVIEW);

    final RevuProjectSettingsForm form = project.getComponent(RevuProjectSettingsForm.class);
    ShowSettingsUtil.getInstance().editConfigurable(project, form, new Runnable()
    {
      public void run()
      {
        if (review != null)
        {
          form.selectReview(review);
        }
      }
    });
  }
}