package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.ReviewItem;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public abstract class ReviewItemColumnInfo<Aspect extends Comparable> extends ColumnInfo<ReviewItem, Aspect>
{
  static enum FilterType
  {
    CONTAINS,
    STARTS_WITH
  }

  private final FilterType filterType;

  public ReviewItemColumnInfo(String name, FilterType filterType)
  {
    super(name);
    this.filterType = filterType;
  }

  @Override
  public Comparator<ReviewItem> getComparator()
  {
    return new Comparator<ReviewItem>()
    {
      public int compare(ReviewItem o1, ReviewItem o2)
      {
        return valueOf(o1).compareTo(valueOf(o2));
      }
    };
  }

  public boolean matchFilter(@NotNull ReviewItem item, @NotNull String filterValue)
  {
    String value = valueOf(item).toString();

    switch (filterType)
    {
      case CONTAINS:
        return (value.indexOf(filterValue) > -1);
      case STARTS_WITH:
        return value.startsWith(filterValue);
      default:
        return false;
    }
  }


  static abstract class ReviewItemDateColumnInfo extends ReviewItemColumnInfo<Date>
  {
    public ReviewItemDateColumnInfo(@NotNull String name)
    {
      super(name, FilterType.CONTAINS);
    }

    @Override
    public TableCellRenderer getRenderer(final ReviewItem reviewItem)
    {
      return new DefaultTableCellRenderer()
      {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
          boolean hasFocus, int row, int column)
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
            return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(valueOf(reviewItem));
          }

          return null;
        }
      };
    }
  }
}
