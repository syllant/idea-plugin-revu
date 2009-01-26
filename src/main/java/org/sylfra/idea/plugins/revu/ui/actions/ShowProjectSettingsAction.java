package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.ui.forms.settings.RevuProjectSettingsForm;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ShowProjectSettingsAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    ShowSettingsUtil.getInstance().showSettingsDialog(project, RevuProjectSettingsForm.class);
  }
}