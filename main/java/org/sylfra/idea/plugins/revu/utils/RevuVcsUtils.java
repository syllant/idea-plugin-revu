package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVcsUtils
{
  private static final Logger LOGGER = Logger.getInstance(RevuVcsUtils.class.getName());

  @Nullable
  public static VcsRevisionNumber getVcsRevisionNumber(@NotNull Project project, @NotNull VirtualFile vFile)
  {
    AbstractVcs vcs = VcsUtil.getVcsFor(project, vFile);
    if (vcs == null)
    {
      return null;
    }

    final FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(vFile);
    if ((filePath == null) || (!vcs.fileIsUnderVcs(filePath)))
    {
      return null;
    }

    DiffProvider diffProvider = vcs.getDiffProvider();
    if (diffProvider == null)
    {
      return null;
    }

    VcsRevisionNumber rev = diffProvider.getCurrentRevision(vFile);
    return (VcsRevisionNumber.NULL.equals(rev)) ? null : rev;
  }
}