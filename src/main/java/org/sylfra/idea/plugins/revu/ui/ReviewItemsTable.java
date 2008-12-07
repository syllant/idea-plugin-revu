package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.SortableColumnModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.RevuDataKeys;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.business.IReviewListener;
import org.sylfra.idea.plugins.revu.business.ReviewManager;
import org.sylfra.idea.plugins.revu.model.Review;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemsTable extends TableView<ReviewItem>
  implements DataProvider, OccurenceNavigator
{
  private final Project project;

  public ReviewItemsTable(@NotNull Project project, List<ReviewItem> items, @Nullable Review review)
  {
    super(new ReviewItemTableModel(project, items, review));
    this.project = project;

    PopupHandler.installPopupHandler(this, "revu.reviewItemTable.popup", "reviewItemTable");

    // Double-Click
    addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        if (e.getClickCount() == 2)
        {
          ActionUtil.execute("revu.JumpToSource", e, ReviewItemsTable.this, "reviewItemTable", 0);
        }
      }
    });
  }

  public boolean hasNextOccurence()
  {
    int currentRow = getSelectedRow();
    return ((currentRow > -1) && (currentRow < getRowCount() - 1));
  }

  public boolean hasPreviousOccurence()
  {
    return (getSelectedRow() > 0);
  }

  public OccurenceInfo goNextOccurence()
  {
    return buildOccurenceInfo(1);
  }

  public OccurenceInfo goPreviousOccurence()
  {
    return buildOccurenceInfo(-1);
  }

  private OccurenceInfo buildOccurenceInfo(final int offset)
  {
    Navigatable navigatable = new Navigatable()
    {
      public void navigate(boolean requestFocus)
      {
        int currentRow = getSelectedRow();
        int newRow = currentRow + offset;
        getSelectionModel().setSelectionInterval(newRow, newRow);
      }

      public boolean canNavigate()
      {
        return true;
      }

      public boolean canNavigateToSource()
      {
        return true;
      }
    };
    return new OccurenceInfo(navigatable, getSelectedRow(), getRowCount());
  }

  public String getNextOccurenceActionName()
  {
    return RevuBundle.message("action.next.description");
  }

  public String getPreviousOccurenceActionName()
  {
    return RevuBundle.message("action.previous.description");
  }

  public Object getData(@NonNls String dataId)
  {
    if (PlatformDataKeys.NAVIGATABLE_ARRAY.getName().equals(dataId))
    {
      ReviewItem currentItem = getSelectedObject();
      if (currentItem != null)
      {
        OpenFileDescriptor fileDescriptor = new OpenFileDescriptor(project, currentItem.getFile(),
          currentItem.getLineStart() - 1, 0);
        return new Navigatable[]{fileDescriptor};
      }

      return null;
    }

    if (RevuDataKeys.REVIEW_ITEM.getName().equals(dataId))
    {
      return getSelectedObject();
    }

    if (RevuDataKeys.REVIEW_ITEM_ARRAY.getName().equals(dataId))
    {
      return getSelection();
    }

    return null;
  }

  private final static class ReviewItemTableModel extends ListTableModel<ReviewItem>
    implements IReviewItemListener
  {
    // ListTableModel does not expose items list (#getItems() provides a copy)
    private java.util.List<ReviewItem> items;

    private static final ColumnInfo[] COLUMN_INFOS =
      {
        new ColumnInfo<ReviewItem, String>(
          RevuBundle.message("browsing.items.table.priority.title"))
        {
          public String valueOf(ReviewItem reviewItem)
          {
            return (reviewItem.getPriority() == null) ? "" : reviewItem.getPriority().getName();
          }

          @Override
          public Comparator<ReviewItem> getComparator()
          {
            return new Comparator<ReviewItem>()
            {
              public int compare(ReviewItem o1, ReviewItem o2)
              {
                String p1 = (o1.getPriority() == null) ? "" : o1.getPriority().getName();
                String p2 = (o2.getPriority() == null) ? "" : o2.getPriority().getName();
                return p1.compareTo(p2);
              }
            };
          }
        },
        new ColumnInfo<ReviewItem, String>(RevuBundle.message("browsing.items.table.title.title"))
        {
          public String valueOf(ReviewItem reviewItem)
          {
            return reviewItem.getSummary();
          }

          @Override
          public TableCellRenderer getRenderer(final ReviewItem reviewItem)
          {
            return new DefaultTableCellRenderer()
            {
              @Override
              public String getToolTipText()
              {
                return (reviewItem == null) ? null : reviewItem.getDesc();
              }
            };
          }

          @Override
          public Comparator<ReviewItem> getComparator()
          {
            return new Comparator<ReviewItem>()
            {
              public int compare(ReviewItem o1, ReviewItem o2)
              {
                return o1.getDesc().compareTo(o2.getDesc());
              }
            };
          }
        },
        new ColumnInfo<ReviewItem, String>(RevuBundle.message("browsing.items.table.user.title"))
        {
          public String valueOf(ReviewItem reviewItem)
          {
            return reviewItem.getHistory().getCreatedBy().getDisplayName();
          }

          @Override
          public Comparator<ReviewItem> getComparator()
          {
            return new Comparator<ReviewItem>()
            {
              public int compare(ReviewItem o1, ReviewItem o2)
              {
                return o1.getHistory().getCreatedBy().getDisplayName().compareTo(
                  o2.getHistory().getCreatedBy().getDisplayName());
              }
            };
          }
        },
        new ColumnInfo<ReviewItem, Date>(RevuBundle.message("browsing.items.table.date.title"))
        {
          public Date valueOf(ReviewItem reviewItem)
          {
            return reviewItem.getHistory().getCreatedOn();
          }

          @Override
          public TableCellRenderer getRenderer(final ReviewItem reviewItem)
          {
            return new DefaultTableCellRenderer()
            {
              @Override
              public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                             boolean hasFocus,
                                                             int row, int column)
              {
                if (value != null)
                {
                  value = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format((Date) value);
                }

                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
              }

              @Override
              public String getToolTipText()
              {
                if (reviewItem != null)
                {
                  return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG)
                    .format(reviewItem.getHistory().getCreatedOn());
                }

                return null;
              }
            };
          }

          @Override
          public Comparator<ReviewItem> getComparator()
          {
            return new Comparator<ReviewItem>()
            {
              public int compare(ReviewItem o1, ReviewItem o2)
              {
                return o1.getHistory().getCreatedOn().compareTo(o2.getHistory().getCreatedOn());
              }
            };
          }
        }
      };

    public ReviewItemTableModel(Project project, java.util.List<ReviewItem> items, Review review)
    {
      super(COLUMN_INFOS, items, 0);
      this.items = items;

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
            ReviewItemTableModel.this.items.addAll(review.getItems());
            resort(ReviewItemTableModel.this.items);
            fireTableDataChanged();
            review.addReviewItemListener(ReviewItemTableModel.this);
          }

          public void reviewDeleted(Review review)
          {
            review.removeReviewItemListener(ReviewItemTableModel.this);
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
    public void setItems(List<ReviewItem> items)
    {
      super.setItems(items);
      this.items = items;
    }

    public void itemAdded(ReviewItem item)
    {
      items.add(item);
      resort(items);
      int index = items.indexOf(item);
      fireTableRowsInserted(index, index);
    }

    public void itemDeleted(ReviewItem item)
    {
      int index = items.indexOf(item);
      items.remove(item);
      fireTableRowsDeleted(index, index);
    }

    public void itemUpdated(ReviewItem item)
    {
      int index = items.indexOf(item);
      fireTableRowsUpdated(index, index);
    }

    // Already defined in ListTableModel, but private...
    private void resort(List<ReviewItem> items)
    {
      int sortedColumnIndex = getSortedColumnIndex();
      if ((sortedColumnIndex >= 0) && (sortedColumnIndex < COLUMN_INFOS.length))
      {
        final ColumnInfo columnInfo = COLUMN_INFOS[sortedColumnIndex];
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
  }
}
