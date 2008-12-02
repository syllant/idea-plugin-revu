package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.CustomAutoScrollToSourceHandler;
import org.sylfra.idea.plugins.revu.ui.ReviewItemsTable;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemTabbedPane;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewBrowsingForm
{
  private JPanel contentPane;
  private final Project project;
  private final Review review;
  private ReviewItemsTable reviewItemsTable;
  private JComponent toolbar;
  private ReviewItemTabbedPane reviewItemTabbedPane;
  private JSplitPane splitPane;

  public ReviewBrowsingForm(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;

    RevuWorkspaceSettings workspaceSettings = project.getComponent(RevuWorkspaceSettingsComponent.class).getState();
    splitPane.setOrientation(Integer.parseInt(workspaceSettings.getToolWindowSplitOrientation()));    
  }

  public JPanel getContentPane()
  {
    return contentPane;
  }

  public JSplitPane getSplitPane()
  {
    return splitPane;
  }

  private void createUIComponents()
  {
    final List<ReviewItem> items = retrieveReviewItems();

    reviewItemTabbedPane = new ReviewItemTabbedPane(project);

    reviewItemsTable = new ReviewItemsTable(project, items, review);
    reviewItemsTable.setSelectionModel(new DefaultListSelectionModel()
    {
      @Override
      protected void fireValueChanged(int firstIndex, int lastIndex, boolean isAdjusting)
      {
        if (isAdjusting)
        {
          if (!beforeChangeReviewItem())
          {
            return;
          }
        }
        else
        {
          afterChangeReviewItem();
        }
        super.fireValueChanged(firstIndex, lastIndex, isAdjusting);
      }
    });

    if (reviewItemsTable.getRowCount() > 0)
    {
      reviewItemsTable.getSelectionModel().setSelectionInterval(0, 0);
      afterChangeReviewItem();
    }

    RevuWorkspaceSettingsComponent workspaceSettingsComponent = project.getComponent(
      RevuWorkspaceSettingsComponent.class);

    CustomAutoScrollToSourceHandler autoScrollToSourceHandler
      = new CustomAutoScrollToSourceHandler(workspaceSettingsComponent.getState());
    autoScrollToSourceHandler.install(reviewItemsTable);

    toolbar = createToolbar().getComponent();
  }

  private boolean beforeChangeReviewItem()
  {
    // @TODO check this
    ReviewItem current = reviewItemsTable.getSelectedObject();
    return (current == null) || reviewItemTabbedPane.updateData(current);
  }

  private void afterChangeReviewItem()
  {
    ReviewItem current = reviewItemsTable.getSelectedObject();
    if (current != null)
    {
      reviewItemTabbedPane.updateUI(current);
    }
  }

  public void updateReview()
  {
    afterChangeReviewItem();
  }

  public void updateReviewItems()
  {
    reviewItemsTable.getListTableModel().setItems(retrieveReviewItems());
  }

  private List<ReviewItem> retrieveReviewItems()
  {
    final List<ReviewItem> items;

    if (review == null)
    {
      items = new ArrayList<ReviewItem>();
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      for (Review review : reviewManager.getReviews())
      {
        if (review.isActive())
        {
          items.addAll(review.getItems());
        }
      }
    }
    else
    {
      items = review.getItems();
    }

    return items;
  }

  private ActionToolbar createToolbar()
  {
    String toolbarId = (review == null)
      ? "revu.toolWindow.allReviews"
      : "revu.toolWindow.review";

    ActionGroup actionGroup = (ActionGroup) ActionManager.getInstance().getAction(toolbarId);

    return ActionManager.getInstance()
      .createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
  }
}
