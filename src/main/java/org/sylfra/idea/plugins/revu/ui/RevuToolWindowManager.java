package org.sylfra.idea.plugins.revu.ui;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManagerAdapter;
import com.intellij.ui.content.ContentManagerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuIconProvider;
import org.sylfra.idea.plugins.revu.RevuKeys;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
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
    contentsByReviews = new IdentityHashMap<Review, Content>();
  }

  private IssueBrowsingPane addReviewTab(@Nullable Review review)
  {
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();

    IssueBrowsingPane issueBrowsingPane = new IssueBrowsingPane(project, review);
    Content content = contentFactory.createContent(issueBrowsingPane.getContentPane(), getTabTitle(review), true);
    content.putUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY, issueBrowsingPane);
    toolwindow.getContentManager().addContent(content);
    contentsByReviews.put(review, content);

    return issueBrowsingPane;
  }

  private void removeReviewTab(@Nullable Review review)
  {
    Content content = contentsByReviews.remove(review);
    if (content != null)
    {
      toolwindow.getContentManager().removeContent(content, true);
    }
  }

  private String getTabTitle(Review review)
  {
    return (review == null)
      ? RevuBundle.message("browsing.issues.allReviews.title")
      : RevuBundle.message("browsing.issues.review.title", review.getName());
  }

  @Nullable
  public IssueBrowsingPane getSelectedReviewBrowsingForm()
  {
    Content selectedContent = toolwindow.getContentManager().getSelectedContent();

    return (selectedContent != null) ? selectedContent.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY) : null;
  }

  public void projectOpened()
  {
    toolwindow = ToolWindowManager.getInstance(project)
      .registerToolWindow(RevuPlugin.PLUGIN_NAME, true, ToolWindowAnchor.BOTTOM);
    toolwindow.setIcon(RevuIconProvider.getIcon(RevuIconProvider.IconRef.REVU));

    toolwindow.getContentManager().addContentManagerListener(new ContentManagerAdapter() {
      @Override
      public void selectionChanged(ContentManagerEvent event)
      {
        IssueBrowsingPane browsingPane = getSelectedReviewBrowsingForm();
        if (browsingPane != null)
        {
          browsingPane.updateUI(false);
        }
      }
    });

    // Add 'All reviews' tab
    addReviewTab(null);

    project.getComponent(ReviewManager.class).addReviewListener(this);
  }

  public void projectClosed()
  {
    // If dispose is done in #initComponent(), userData is empty ?! 
    for (Content content : contentsByReviews.values())
    {
      IssueBrowsingPane pane = content.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY);
      if (pane != null)
      {
        pane.dispose();
      }
    }

    project.getComponent(ReviewManager.class).removeReviewListener(this);
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

  public ToolWindow getToolwindow()
  {
    return toolwindow;
  }

  public void reviewChanged(Review review)
  {
    if (!RevuUtils.isActive(review))
    {
      removeReviewTab(review);
    }
    else
    {
      Content content = contentsByReviews.get(review);
      if (content == null)
      {
        addReviewTab(review);
      }
      else
      {
        content.setDisplayName(getTabTitle(review));
        // @TODO how to refresh tab title ?
        IssueBrowsingPane pane = content.getUserData(RevuKeys.ISSUE_BROWSING_PANE_KEY);
        if (pane != null)
        {
          pane.updateReview();
        }
      }
    }
  }

  public void reviewAdded(Review review)
  {
    if (RevuUtils.isActive(review))
    {
      addReviewTab(review);
    }
  }

  public void reviewDeleted(Review review)
  {
    removeReviewTab(review);
  }
}
