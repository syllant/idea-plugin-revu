package org.sylfra.idea.plugins.revu.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id: ShowProjectSettingsAction.java 25 2010-04-15 19:13:05Z syllant $
 */
public class ShowAppSettingsAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    RevuUtils.editAppSettings(e.getData(DataKeys.PROJECT));
  }
}