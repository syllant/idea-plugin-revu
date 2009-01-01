package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.sylfra.idea.plugins.revu.model.Issue;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.lang.reflect.ParameterizedType;
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
  private final int preferredWidth;
  private final int minWidth;
  private final int maxWidth;
  private int horizontalAlignment;

  public IssueColumnInfo(@NotNull String name, @NotNull FilterType filterType,
    int preferredWidth, int minWidth, int maxWidth)
  {
    super(name);
    this.filterType = filterType;
    this.preferredWidth = preferredWidth;
    this.minWidth = minWidth;
    this.maxWidth = maxWidth;

    Class aspectClass = (Class) ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    horizontalAlignment = (Number.class.isAssignableFrom(aspectClass) || Date.class.isAssignableFrom(aspectClass))
      ? SwingConstants.CENTER : SwingConstants.LEFT;
  }

  @Override
  public int getWidth(JTable table)
  {
    return preferredWidth;
  }

  public int getMinWidth(JTable table)
  {
    return minWidth;
  }

  public int getMaxWidth(JTable table)
  {
    return maxWidth;
  }

  @Override
  public TableCellRenderer getRenderer(final Issue issue)
  {
    return new DefaultTableCellRenderer()
    {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
        int row, int column)
      {
        value = IssueColumnInfo.this.formatValue((Aspect) value);

        JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        result.setHorizontalAlignment(IssueColumnInfo.this.getHorizontalAlignment());

        customizeRenderer(result, (String) value, isSelected, hasFocus);

        return result;
      }

      @Override
      public String getToolTipText()
      {
        String tip = IssueColumnInfo.this.getToolTipText(issue);
        return ("".equals(tip)) ? null : tip;
      }
    };
  }

  protected void customizeRenderer(JLabel result, String value, boolean selected, boolean hasFocus)
  {
  }

  protected String formatValue(Aspect value)
  {
    return (value == null) ? "" : value.toString();
  }

  protected int getHorizontalAlignment()
  {
    return horizontalAlignment;
  }

  protected String getToolTipText(Issue issue)
  {
    String result = formatValue(valueOf(issue));
    return ("".equals(result)) ? null : result;
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

  public boolean matchFilter(@NotNull Issue issue, @NotNull String filterValue)
  {
    String value = valueOf(issue).toString();

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


  static abstract class IssueDateColumnInfo<Aspect extends Date> extends IssueColumnInfo<Aspect>
  {
    public IssueDateColumnInfo(@NotNull String name)
    {
      super(name, FilterType.CONTAINS, 80, 50, 80);
    }

    @Override
    protected String formatValue(Date value)
    {
      return (value == null) ? null : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(value);
    }

    @Override
    protected String getToolTipText(Issue issue)
    {
      return (issue == null) ? null
        : DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(valueOf(issue));
    }
  }
}
