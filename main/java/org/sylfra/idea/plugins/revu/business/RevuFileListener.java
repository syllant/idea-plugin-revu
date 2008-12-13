package org.sylfra.idea.plugins.revu.business;

import com.intellij.AppTopics;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.fileEditor.FileDocumentManagerAdapter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuVfsUtils;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuFileListener implements ProjectComponent
{
  private final Project project;
  private final VirtualFileListener virtualFileListener;
  private final FileDocumentManagerAdapter fileDocumentManagerListener;
  private MessageBusConnection messageBusConnection;

  public RevuFileListener(final Project project)
  {
    this.project = project;

    virtualFileListener = new VirtualFileAdapter()
    {
      @Override
      public void contentsChanged(VirtualFileEvent event)
      {
        if (event.isFromSave())
        {
          VirtualFile vFile = event.getFile();
          ReviewManager reviewManager = project.getComponent(ReviewManager.class);

          // Review file
          Review review = reviewManager.getReviewByPath(RevuVfsUtils.buildRelativePath(project, vFile));
          if (review != null)
          {
            reviewManager.load(review, false);
          }
        }
      }
    };

    fileDocumentManagerListener = new FileDocumentManagerAdapter() {
      @Override
      public void beforeAllDocumentsSaving()
      {
        // File with issues
        ReviewManager reviewManager = project.getComponent(ReviewManager.class);
        reviewManager.saveChangedReviews();
      }
    };
    messageBusConnection = this.project.getMessageBus().connect();
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
    LocalFileSystem.getInstance().addVirtualFileListener(virtualFileListener);
    messageBusConnection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, fileDocumentManagerListener);
  }

  public void disposeComponent()
  {
    LocalFileSystem.getInstance().removeVirtualFileListener(virtualFileListener);
    messageBusConnection.disconnect();
  }
}
