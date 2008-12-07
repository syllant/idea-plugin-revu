package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuFileListener extends VirtualFileAdapter implements ProjectComponent
{
  private final Project project;

  public RevuFileListener(Project project)
  {
    this.project = project;
  }

  @Override
  public void contentsChanged(VirtualFileEvent event)
  {
    if (event.isFromSave())
    {
      VirtualFile file = event.getFile();
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);

      // Review file
      Review review = reviewManager.getReview(RevuVfsUtils.buildRelativePath(project, file));
      if (review != null)
      {
        reviewManager.load(review, false);
        return;
      }
    }
  }

  public void projectOpened()
  {
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".RevuFileListener";
  }

  public void initComponent()
  {
    LocalFileSystem.getInstance().addVirtualFileListener(this);
  }

  public void disposeComponent()
  {
    LocalFileSystem.getInstance().removeVirtualFileListener(this);
  }
}
