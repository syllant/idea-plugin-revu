package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Issue;

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
public abstract class IssueColumnInfo<Aspect extends Comparable> extends ColumnInfo<Issue, Aspect>
{
  static enum FilterType
  {
    CONTAINS,
    STARTS_WITH
  }

  private final FilterType filterType;

  public IssueColumnInfo(String name, FilterType filterType)
  {
    super(name);
    this.filterType = filterType;
  }

  @Override
  public Comparator<Issue> getComparator()
  {
    return new Comparator<Issue>()
    {
      public int compare(Issue o1, Issue o2)
      {
        return valueOf(o1).compareTo(valueOf(o2));
      }
    };
  }

  public boolean matchFilter(@NotNull Issue item, @NotNull String filterValue)
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


  static abstract class IssueDateColumnInfo extends IssueColumnInfo<Date>
  {
    public IssueDateColumnInfo(@NotNull String name)
    {
      super(name, FilterType.CONTAINS);
    }

    @Override
    public TableCellRenderer getRenderer(final Issue issue)
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
          if (issue != null)
          {
            return DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(valueOf(issue));
          }

          return null;
        }
      };
    }
  }
}
