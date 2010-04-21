package org.sylfra.idea.plugins.revu.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.pom.Navigatable;
import com.intellij.util.OpenSourceUtil;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class JumpToSourceAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    OpenSourceUtil.openSourcesFrom(e.getDataContext(), true);
  }

  @Override
  public void update(AnActionEvent e)
  {
    boolean enabled = false;

    Navigatable[] navigatables = e.getData(PlatformDataKeys.NAVIGATABLE_ARRAY);
    if (navigatables != null)
    {
      for (Navigatable navigatable : navigatables)
      {
        if (navigatable.canNavigateToSource())
        {
          enabled = true;
          break;
        }
      }
    }

    e.getPresentation().setEnabled(enabled);
  }
}