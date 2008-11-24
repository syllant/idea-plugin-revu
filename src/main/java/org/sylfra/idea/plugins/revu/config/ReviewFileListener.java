package org.sylfra.idea.plugins.revu.config;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;

import java.io.File;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewFileListener extends VirtualFileAdapter implements ProjectComponent
{
  private final Project project;

  public ReviewFileListener(@NotNull Project project)
  {
    this.project = project;
  }

  /**
   * VirtualFileListener implementation : when a review file is modified, review is reloaded
   *
   * @param event a file event
   */
  @Override
  public void contentsChanged(VirtualFileEvent event)
  {
    if (event.isFromSave())
    {
      VirtualFile file = event.getFile();
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      Review review = reviewManager.getReview(new File(file.getPath()));
      if (review != null)
      {
        reviewManager.reload(review);
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
    return RevuPlugin.PLUGIN_NAME + ".ReviewFileListener";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }
}
