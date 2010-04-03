package org.sylfra.idea.plugins.revu.ui.actions.filter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.model.Filter;
import org.sylfra.idea.plugins.revu.ui.forms.filter.FilterSettingsForm;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class EditFiltersAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    final Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return;
    }

    final FilterSettingsForm form = new FilterSettingsForm(project);
    ShowSettingsUtil.getInstance().editConfigurable(project, form, new Runnable()
    {
      public void run()
      {
        Filter selectedFilter = RevuUtils.getWorkspaceSettings(project).getSelectedFilter();
        if (selectedFilter != null)
        {
          form.selectItem(selectedFilter);
        }
      }
    });
  }
}