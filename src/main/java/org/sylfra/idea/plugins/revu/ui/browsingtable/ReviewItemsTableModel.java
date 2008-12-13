package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.openapi.project.Project;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.SortableColumnModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettings;
import org.sylfra.idea.plugins.revu.settings.project.workspace.RevuWorkspaceSettingsComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
* @version $Id$
*/
public final class ReviewItemsTableModel extends ListTableModel<ReviewItem>
  implements IReviewItemListener
{
  // ListTableModel does not expose items list (#getItems() provides a copy)
  private java.util.List<ReviewItem> allItems;
  private java.util.List<ReviewItem> visibleItems;

  public ReviewItemsTableModel(@NotNull Project project, @NotNull java.util.List<ReviewItem> items,
    @Nullable Review review)
  {
    super(retrieveColumnsFromSettings(project), items, 0);
    visibleItems = items;
    allItems = new ArrayList<ReviewItem>(items);

    setSortable(true);

    if (review != null)
    {
      review.addReviewItemListener(this);
    }
    else
    {
      ReviewManager reviewManager = project.getComponent(ReviewManager.class);
      reviewManager.addReviewListener(new IReviewListener()
      {
        public void reviewChanged(Review review)
        {
        }

        public void reviewAdded(Review review)
        {
          allItems.addAll(review.getItems());
          visibleItems.addAll(review.getItems());
          resort(visibleItems);

          fireTableDataChanged();
          review.addReviewItemListener(ReviewItemsTableModel.this);
        }

        public void reviewDeleted(Review review)
        {
          review.removeReviewItemListener(ReviewItemsTableModel.this);
        }
      });
      Collection<Review> reviews = reviewManager.getReviews();
      for (Review aReview : reviews)
      {
        aReview.addReviewItemListener(this);
      }
    }
  }

  @Override
  public void setItems(java.util.List<ReviewItem> items)
  {
    super.setItems(items);
    visibleItems = items;
    allItems = new ArrayList<ReviewItem>(items);
  }

  public void itemAdded(ReviewItem item)
  {
    allItems.add(item);
    visibleItems.add(item);

    resort(visibleItems);
    int index = visibleItems.indexOf(item);
    fireTableRowsInserted(index, index);
  }

  public void itemDeleted(ReviewItem item)
  {
    allItems.remove(item);

    int index = visibleItems.indexOf(item);
    if (index > -1)
    {
      visibleItems.remove(item);
      fireTableRowsDeleted(index, index);
    }
  }

  public void itemUpdated(ReviewItem item)
  {
    int index = visibleItems.indexOf(item);
    if (index > -1)
    {
      fireTableRowsUpdated(index, index);
    }
  }

  // Already defined in ListTableModel, but private...
  private void resort(java.util.List<ReviewItem> items)
  {
    int sortedColumnIndex = getSortedColumnIndex();
    if ((sortedColumnIndex >= 0) && (sortedColumnIndex < ReviewItemColumnInfoRegistry.ALL_COLUMN_INFOS.length))
    {
      final ColumnInfo columnInfo = ReviewItemColumnInfoRegistry.ALL_COLUMN_INFOS[sortedColumnIndex];
      if (columnInfo.isSortable())
      {
        //noinspection unchecked
        columnInfo.sort(items);
        if (getSortingType() == SortableColumnModel.SORT_DESCENDING)
        {
          reverseModelItems(items);
        }

        fireTableDataChanged();
      }
    }
  }

  public void filter(@NotNull String filter)
  {
    visibleItems.clear();

    if (filter.length() == 0)
    {
      visibleItems.addAll(allItems);
    }
    else
    {
      for (ReviewItem item : allItems)
      {
        boolean match = false;
        for (ReviewItemColumnInfo columnInfo : ReviewItemColumnInfoRegistry.ALL_COLUMN_INFOS)
        {
          if (columnInfo.matchFilter(item, filter))
          {
            match = true;
            break;
          }
        }

        if (match)
        {
          visibleItems.add(item);
        }
      }
    }
  }

  private static ColumnInfo[] retrieveColumnsFromSettings(@NotNull Project project)
  {
    RevuWorkspaceSettings workspaceSettings = project.getComponent(RevuWorkspaceSettingsComponent.class).getState();

    List<String> colNames = workspaceSettings.getBrowsingColNames();
    List<ColumnInfo> result = new ArrayList<ColumnInfo>(colNames.size());

    for (String colName : colNames)
    {
      ReviewItemColumnInfo columnInfo = ReviewItemColumnInfoRegistry.ALL_COLUMN_INFOS_BY_NAMES.get(colName);
      if (columnInfo != null)
      {
        result.add(columnInfo);
      }
    }
    
    return result.toArray(new ColumnInfo[result.size()]);
  }
}
