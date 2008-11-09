package org.sylfra.idea.plugins.revu.ui.forms;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuPlugin;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.settings.RevuSettingsComponent;
import org.sylfra.idea.plugins.revu.ui.CustomAutoScrollToSourceHandler;
import org.sylfra.idea.plugins.revu.ui.ReviewItemsTable;
import org.sylfra.idea.plugins.revu.ui.forms.reviewitem.ReviewItemTabbedPane;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
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

  public ReviewBrowsingForm(@NotNull Project project, @Nullable Review review)
  {
    this.project = project;
    this.review = review;
  }

  public JPanel getContentPane()
  {
    return contentPane;
  }

  private void createUIComponents()
  {
    final java.util.List<ReviewItem> items = retrieveReviewItems();

    reviewItemTabbedPane = new ReviewItemTabbedPane(project);

    reviewItemsTable = new ReviewItemsTable(project, items);
    reviewItemsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(final ListSelectionEvent e)
      {
        if (!e.getValueIsAdjusting())
        {
          ReviewItem current = reviewItemsTable.getSelectedObject();
          if (current != null)
          {
            reviewItemTabbedPane.updateUI(current);
          }
        }
      }
    });

    RevuSettingsComponent settingsComponent = ServiceManager.getService(project,
      RevuSettingsComponent.class);

    CustomAutoScrollToSourceHandler autoScrollToSourceHandler
      = new CustomAutoScrollToSourceHandler(settingsComponent.getState());
    autoScrollToSourceHandler.install(reviewItemsTable);

    toolbar = createToolbar().getComponent();
  }

  private java.util.List<ReviewItem> retrieveReviewItems()
  {
    final java.util.List<ReviewItem> items;

    if (review == null)
    {
      items = new ArrayList<ReviewItem>();
      ReviewManager reviewManager = ServiceManager.getService(project, ReviewManager.class);
      for (Review review : reviewManager.getReviews())
      {
        items.addAll(review.getItems());
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
      .createActionToolbar(RevuPlugin.PLUGIN_NAME, actionGroup, false);
  }
}
