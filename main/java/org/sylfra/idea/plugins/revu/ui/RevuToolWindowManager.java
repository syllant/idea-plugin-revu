package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.ui.forms.ReviewBrowsingForm;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuToolWindowManager implements ProjectComponent, IReviewListener
{
  private ToolWindow toolwindow;
  private final Project project;
  private final Map<Review, Content> contentsByReviews;

  public RevuToolWindowManager(Project project)
  {
    this.project = project;
    contentsByReviews = new HashMap<Review, Content>();
  }

  private void addReviewTab(@Nullable Review review)
  {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    String title = (review == null)
      ? RevuBundle.message("toolwindow.allReviews.title")
      : RevuBundle.message("toolwindow.review.title", review.getTitle());

    Content content = contentFactory.createContent(
      new ReviewBrowsingForm(project, review).getContentPane(), title, true);
    toolwindow.getContentManager().addContent(content);
    contentsByReviews.put(review, content);
  }

  private void removeReviewTab(@Nullable Review review)
  {
    Content content = contentsByReviews.remove(review);
    toolwindow.getContentManager().removeContent(content, true);
  }

  public void projectOpened()
  {
    toolwindow = ToolWindowManager.getInstance(project)
      .registerToolWindow(RevuPlugin.PLUGIN_NAME, true, ToolWindowAnchor.BOTTOM);
    toolwindow.setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU));

    addReviewTab(null);
    ReviewManager reviewManager = ServiceManager.getService(project, ReviewManager.class);
    for (Review review : reviewManager.getReviews())
    {
      addReviewTab(review);
    }
  }

  public void projectClosed()
  {
  }

  @NotNull
  public String getComponentName()
  {
    return RevuPlugin.PLUGIN_NAME + ".ToolWindowManager";
  }

  public void initComponent()
  {
  }

  public void disposeComponent()
  {
  }

  public void reviewAdded(Review review)
  {
    addReviewTab(review);
  }

  public void reviewDeleted(Review review)
  {
    removeReviewTab(review);
  }
}
