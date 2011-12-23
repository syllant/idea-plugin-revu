package org.sylfra.idea.plugins.revu.ui.projectView;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.*;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
* @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
* @version $Id$
*/
public class RevuPackageSet extends PackageSetBase
{
  private static final Logger LOGGER = Logger.getInstance(RevuPackageSet.class.getName());

  private final Project project;
  private final Review review;
  private final FileScopeManager fileScopeManager;
  private final PackageSet wrappedPackageSet;

  public RevuPackageSet(@NotNull Project project, @NotNull Review review)
  {
    this.project = project;
    this.review = review;
    fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);

    PackageSet packageSetTmp = null;
    String pathPattern = review.getFileScope().getPathPattern();
    try
    {
      if (pathPattern != null)
      {
        packageSetTmp = PackageSetFactory.getInstance().compile(pathPattern);
      }
    }
    catch (ParsingException e)
    {
      LOGGER.warn("Failed to compile file scope path pattern: <" + pathPattern + ">");
    }

    wrappedPackageSet = packageSetTmp;
  }

  public PackageSet getWrappedPackageSet()
  {
    return wrappedPackageSet;
  }

  @Override
  public boolean contains(VirtualFile vFile, NamedScopesHolder holder)
  {
    return (vFile != null)
      && (checkFilter(vFile))
      && fileScopeManager.belongsToScope(project, review, wrappedPackageSet, vFile);
  }

  private boolean checkFilter(VirtualFile vFile)
  {
    return !RevuUtils.getWorkspaceSettings(project).isFilterFilesWithIssues() || review.hasIssues(vFile);
  }

  public PackageSet createCopy()
  {
    return this;
  }

  public String getText()
  {
    return "";
  }

  public int getNodePriority()
  {
    return 0;
  }
}
