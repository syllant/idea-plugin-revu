package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder;
import com.intellij.psi.search.scope.packageSet.PackageSet;
import com.intellij.psi.search.scope.packageSet.PackageSetBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.FileScope;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.projectView.RevuPackageSet;
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

  public boolean belongsToScope(@NotNull Project project, @NotNull Review review, @NotNull VirtualFile vFile)
  {
    RevuPackageSet revuPackageSet = new RevuPackageSet(project, review);
    return belongsToScope(project, review, revuPackageSet.getWrappedPackageSet(), vFile);
  }

  public boolean belongsToScope(@NotNull Project project, @NotNull Review review, @Nullable PackageSet packageSet,
    @NotNull VirtualFile vFile)
  {
    return matchFrom(project, review.getFileScope(), vFile) && matchPathPattern(project, review, packageSet, vFile);
  }

  public boolean matchFrom(Project project, FileScope fileScope, VirtualFile vFile)
  {
    if ((fileScope.getVcsBeforeRev() == null) && (fileScope.getVcsAfterRev() == null))
    {
      return true;
    }

    return matchFrom(project, fileScope, vFile, RevuVcsUtils.getVcsRevisionNumber(project, vFile));
  }

  public boolean matchFrom(Project project, FileScope fileScope, VirtualFile vFile, VcsRevisionNumber rev)
  {
    if (rev == null)
    {
      return true;
    }

    if ((fileScope.getVcsBeforeRev() == null) && (fileScope.getVcsAfterRev() == null))
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
      if ((fileScope.getVcsBeforeRev() != null)
        && (rev.compareTo(vcs.parseRevisionNumber(fileScope.getVcsBeforeRev())) < 0))
      {
        return false;
      }

      return ((fileScope.getVcsAfterRev() == null)
        || (rev.compareTo(vcs.parseRevisionNumber(fileScope.getVcsAfterRev())) >= 0));
    }
    catch (Exception ignored)
    {
      if (LOGGER.isDebugEnabled())
      {
        LOGGER.debug("Failed to parse VCS rev: " + fileScope.getVcsBeforeRev());
      }
      return true;
    }
  }

  private boolean matchPathPattern(@NotNull Project project, @NotNull Review review, @Nullable PackageSet packageSet,
    @NotNull VirtualFile vFile)
  {
    if (packageSet == null)
    {
      return true;
    }

    // Does not work anymore with IDEA 11
    // final NamedScopesHolder holder = NamedScopesHolder.getHolder(project, review.getName(), null);
    final NamedScopesHolder holder = NamedScopesHolder.getAllNamedScopeHolders(project)[0];

    return (holder == null) || (((PackageSetBase) packageSet).contains(vFile, holder));
  }
}
