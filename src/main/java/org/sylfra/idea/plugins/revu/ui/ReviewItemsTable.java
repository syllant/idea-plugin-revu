package org.sylfra.idea.plugins.revu.ui;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.business.IReviewItemListener;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewItemsTable extends TableView<ReviewItem>
  implements DataProvider, OccurenceNavigator
{
  private final Project project;

  public ReviewItemsTable(@NotNull Project project, List<ReviewItem> items)
  {
    super(new ReviewItemTableModel(items));
    this.project = project;
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
          currentItem.getLineStart());
        return new Navigatable[]{fileDescriptor};
      }

      return null;
    }

    return null;
  }

  private final static class ReviewItemTableModel extends ListTableModel<ReviewItem>
    implements IReviewItemListener
  {
    private static final ColumnInfo[] COLUMN_INFOS =
      {
        new ColumnInfo<ReviewItem, String>(
          RevuBundle.message("browsing.items.table.priority.title"))
        {
          public String valueOf(ReviewItem reviewItem)
          {
            return reviewItem.getPriority().getName();
          }

          @Override
          public Comparator<ReviewItem> getComparator()
          {
            return new Comparator<ReviewItem>()
            {
              public int compare(ReviewItem o1, ReviewItem o2)
              {
                return o1.getPriority().getName().compareTo(o2.getPriority().getName());
              }
            };
          }
        },
        new ColumnInfo<ReviewItem, String>(RevuBundle.message("browsing.items.table.title.title"))
        {
          public String valueOf(ReviewItem reviewItem)
          {
            return reviewItem.getTitle();
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
            return new Date(reviewItem.getHistory().getCreatedOn());
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
                return (int) (o1.getHistory().getCreatedOn() - o2.getHistory().getCreatedOn());
              }
            };
          }
        }
      };
    private final java.util.List<ReviewItem> items;

    public ReviewItemTableModel(java.util.List<ReviewItem> items)
    {
      super(COLUMN_INFOS, items, 0);
      this.items = items;
      setSortable(true);
    }

    public void itemAdded(ReviewItem item)
    {
      int index = items.indexOf(item);
      fireTableRowsInserted(index, index);
    }

    public void itemDeleted(ReviewItem item)
    {
      int index = items.indexOf(item);
      fireTableRowsDeleted(index, index);
    }
  }

}
