package org.sylfra.idea.plugins.revu.ui.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.util.OpenSourceUtil;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class JumpToSourceAction extends AnAction
{
  public void actionPerformed(AnActionEvent e)
  {
    VirtualFile vFile = e.getData(DataKeys.VIRTUAL_FILE);
    if (vFile != null)
    {
      if (FileTypeManager.getInstance().getFileTypeByFile(vFile) == FileTypes.UNKNOWN)
      {
        return;
      }
    }

    Navigatable[] navigatables = e.getData(PlatformDataKeys.NAVIGATABLE_ARRAY);
    if (navigatables != null)
    {
      for (Navigatable navigatable : navigatables)
      {
        if (!navigatable.canNavigateToSource())
        {
          return;
        }
      }
    }

    OpenSourceUtil.openSourcesFrom(e.getDataContext(), true);
  }
}