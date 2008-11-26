package org.sylfra.idea.plugins.revu.business;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileAdapter;
import com.intellij.openapi.vfs.VirtualFileEvent;
import org.sylfra.idea.plugins.revu.model.Review;

import java.io.File;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuVirtualFileListener extends VirtualFileAdapter
{
  private final Project project;

  public RevuVirtualFileListener(Project project)
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
      Review review = reviewManager.getReview(new File(file.getPath()));
      if (review != null)
      {
        reviewManager.reload(review, false);
      }
    }
  }
}
