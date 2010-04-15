package org.sylfra.idea.plugins.revu.ui.projectView;

import com.intellij.ide.SelectInContext;
import com.intellij.ide.impl.ProjectViewSelectInTarget;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.FileScopeManager;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
class RevuPaneSelectInTarget extends ProjectViewSelectInTarget
{
  private final ReviewManager reviewManager;
  private final FileScopeManager fileScopeManager;

  public RevuPaneSelectInTarget(final Project project)
  {
    super(project);
    reviewManager = project.getComponent(ReviewManager.class);
    fileScopeManager = ApplicationManager.getApplication().getComponent(FileScopeManager.class);
  }

  @Override
  public String toString()
  {
    return RevuBundle.message("general.plugin.title");
  }

  public String getMinorViewId()
  {
    return RevuProjectViewPane.ID;
  }

  public float getWeight()
  {
    return Integer.MAX_VALUE;
  }

  public boolean canSelect(PsiFileSystemItem fileSystemItem)
  {
    if (!super.canSelect(fileSystemItem) || !(fileSystemItem instanceof PsiFile))
    {
      return false;
    }
    PsiFile file = (PsiFile) fileSystemItem;

    VirtualFile vFile = file.getVirtualFile();
    if (vFile == null)
    {
      return false;
    }

    for (Review review : RevuUtils.getActiveReviewsForCurrentUser(myProject))
    {
      if (fileScopeManager.belongsToScope(myProject, review, vFile))
      {
        return true;
      }
    }

    return false;
  }

  public boolean isSubIdSelectable(String subId, SelectInContext context)
  {
    if ((context == null) || (subId == null))
    {
      return false;
    }

    Review review = reviewManager.getReviewByName(subId);
    return (review != null) && (fileScopeManager.belongsToScope(myProject, review, context.getVirtualFile()));
  }

}
