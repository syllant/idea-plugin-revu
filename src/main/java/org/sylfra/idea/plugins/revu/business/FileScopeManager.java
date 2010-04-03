package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.FileScope;
import org.sylfra.idea.plugins.revu.utils.RevuVcsUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class FileScopeManager implements ApplicationComponent
{
  private static final Logger LOGGER = Logger.getInstance(FileScopeManager.class.getName());

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + "." + getClass().getSimpleName();
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }

  public boolean belongsToScope(@NotNull Project project, @NotNull FileScope fileScope, @NotNull VirtualFile vFile)
  {
    if (fileScope.getDate() != null)
    {
      return vFile.getModificationStamp() >= fileScope.getDate().getTime();
    }

    if (fileScope.getRev() != null)
    {
      VcsRevisionNumber rev = RevuVcsUtils.getVcsRevisionNumber(project, vFile);
      if (rev == null)
      {
        return true;
      }

      AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(vFile);
      if (vcs == null)
      {
        return true;
      }

      try
      {
        return rev.compareTo(vcs.parseRevisionNumber(fileScope.getRev())) >= 0;
      }
      catch (Exception ignored)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug("Failed to parse VCS rev: " + fileScope.getRev());
        }
        return true;
      }
    }

    return true;
  }
}
