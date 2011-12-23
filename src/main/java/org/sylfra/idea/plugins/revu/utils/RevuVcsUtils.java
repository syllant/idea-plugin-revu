package org.sylfra.idea.plugins.revu.utils;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.actions.VcsContextFactory;
import com.intellij.openapi.vcs.changes.committed.ChangesBrowserDialog;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesTableModel;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.vcsUtil.VcsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVcsUtils
{
  private static final Logger LOGGER = Logger.getInstance(RevuVcsUtils.class.getName());
  private static final int RETRIEVED_CHANGES_MAX_COUNT = 50;

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

    AbstractVcs[] vcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    if (vcss.length == 0)
    {
      return false;
    }

    // @TODO handle case where projet has several VCS roots
    // Here, I use the first VCS connection
    AbstractVcs vcs = vcss[0];
    if ((vcs == null) || (vcs.getCommittedChangesProvider() == null))
    {
      return false;
    }

    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(vFile);

    return AbstractVcs.fileInVcsByFileStatus(project, filePath);
  }

  @Nullable
  public static CommittedChangeList chooseCommittedChangeList(@NotNull Project project)
    throws VcsException
  {
    AbstractVcs[] vcss = ProjectLevelVcsManager.getInstance(project).getAllActiveVcss();
    assert (vcss.length > 0);

    // @TODO handle case where projet has several VCS roots
    // Here, I use the first VCS connection
    AbstractVcs vcs = vcss[0];
    if ((vcs == null) || (vcs.getCommittedChangesProvider() == null))
    {
      return null;
    }

    CommittedChangesProvider provider = vcs.getCommittedChangesProvider();
    assert (provider != null);

    List<CommittedChangeList> changes = new ArrayList<CommittedChangeList>();
    List<VcsDirectoryMapping> mappings = ProjectLevelVcsManager.getInstance(project).getDirectoryMappings(vcs);
    for (VcsDirectoryMapping mapping : mappings)
    {
      VirtualFile vFile;
      if (mapping.isDefaultMapping())
      {
        vFile = project.getBaseDir();
      }
      else
      {
        vFile = LocalFileSystem.getInstance().findFileByPath(mapping.getDirectory());
        if (vFile == null)
        {
          vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(mapping.getDirectory());
        }
      }
      changes.addAll(retrieveChanges(vFile, provider));
    }

    final ChangesBrowserDialog dialog = new ChangesBrowserDialog(project,
      new CommittedChangesTableModel(changes, provider.getColumns(), false), ChangesBrowserDialog.Mode.Choose, null);
    dialog.show();

    return dialog.isOK() ? dialog.getSelectedChangeList() : null;
  }

  private static List<CommittedChangeList> retrieveChanges(VirtualFile vFile,
    CommittedChangesProvider provider) throws VcsException
  {
    FilePath filePath = VcsContextFactory.SERVICE.getInstance().createFilePathOn(vFile);
    assert (filePath != null);

    RepositoryLocation location = provider.getLocationFor(filePath);

    //noinspection unchecked
    return provider.getCommittedChanges(provider.createDefaultSettings(), location, RETRIEVED_CHANGES_MAX_COUNT);
  }

  public static boolean isRevisionNumberParsable(AbstractVcs vcs, String revisionNumber)
  {
    try
    {
      vcs.parseRevisionNumber(revisionNumber);
      return true;
    }
    catch (VcsException e)
    {
      return false;
    }
  }
}