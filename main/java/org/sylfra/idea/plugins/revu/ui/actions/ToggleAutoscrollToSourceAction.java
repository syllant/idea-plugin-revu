package org.sylfra.idea.plugins.revu.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.sylfra.idea.plugins.revu.settings.RevuSettings;
import org.sylfra.idea.plugins.revu.settings.RevuSettingsComponent;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ToggleAutoscrollToSourceAction extends ToggleAction
{
  public boolean isSelected(AnActionEvent e)
  {
    RevuSettings revuSettings = retrieveSettings(e);
    return ((revuSettings != null) && (revuSettings.isAutoScrollToSource()));
  }

  public void setSelected(AnActionEvent e, boolean state)
  {
    RevuSettings revuSettings = retrieveSettings(e);

    revuSettings.setAutoScrollToSource(!revuSettings.isAutoScrollToSource());
  }

  private RevuSettings retrieveSettings(AnActionEvent e)
  {
    Project project = e.getData(DataKeys.PROJECT);
    if (project == null)
    {
      return null;
    }

    RevuSettingsComponent settingsComponent = ServiceManager.getService(project,
      RevuSettingsComponent.class);

    return settingsComponent.getState();
  }
}
