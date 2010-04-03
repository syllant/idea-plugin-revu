package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVcsUtils
{
  private static final Logger LOGGER = Logger.getInstance(RevuVcsUtils.class.getName());

  @Nullable
  public static DiffProvider getDiffProvider(@NotNull Project project, @NotNull VirtualFile vFile)
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

    return vcs.getDiffProvider();
  }

  @Nullable
  public static VcsRevisionNumber getVcsRevisionNumber(@NotNull Project project, @NotNull VirtualFile vFile)
  {
    DiffProvider diffProvider = getDiffProvider(project, vFile);
    if (diffProvider == null)
    {
      return null;
    }

    VcsRevisionNumber rev = diffProvider.getCurrentRevision(vFile);

    return (VcsRevisionNumber.NULL.equals(rev)) ? null : rev;
  }

  @Nullable
  public static boolean isUnderVcs(@NotNull Project project, @Nullable VirtualFile vFile)
  {
    return ((vFile != null) && (VcsUtil.getVcsFor(project, vFile) != null));
  }

  public static boolean fileIsModifiedFromVcs(@NotNull Project project, @NotNull VirtualFile vFile)
  {
    return !FileStatusManager.getInstance(project).getStatus(vFile).equals(FileStatus.NOT_CHANGED);
  }

  @NotNull
  public static String formatRevision(@Nullable VcsRevisionNumber number, boolean currentIdModified)
  {
    return formatRevision(((number == null) || (VcsRevisionNumber.NULL.equals(number))) ? null : number.asString(),
      currentIdModified);
  }

  @NotNull
  public static String formatRevision(@Nullable String number, boolean currentIdModified)
  {
    if ((number == null) || (number.trim().length() == 0))
    {
      return RevuBundle.message("general.vcsRev.text", RevuBundle.message("general.workingCopy.text"));
    }

    return RevuBundle.message(currentIdModified ? "general.vcsRevModified.text" : "general.vcsRev.text", number);
  }

  @NotNull
  public static String formatVcsFileRevision(@NotNull VirtualFile vFile, @NotNull String number)
  {
    return RevuBundle.message("general.vcsFileAndRev.text", vFile.getPath(), number);
  }

  public static boolean mayBrowseChangeLists(@NotNull Project project)
  {
    VirtualFile vFile = project.getBaseDir();
    if (vFile == null)
    {
      return false;
    }

    AbstractVcs vcs = ProjectLevelVcsManager.getInstance(project).getVcsFor(vFile);
    if (vcs == null || vcs.getCommittedChangesProvider() == null)
    {
      return false;
    }

    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(vFile);

    return AbstractVcs.fileInVcsByFileStatus(project, filePath);
  }
}