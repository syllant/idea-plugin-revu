package org.sylfra.idea.plugins.revu.ui.browsingtable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.externalizing.impl.ConverterUtils;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueColumnInfoRegistry
{
  private static final IssueColumnInfo<String> COL_CREATED_BY =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.items.table.createdBy.title"),
      IssueColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getCreatedBy().getDisplayName();
      }
    };

  private static final IssueColumnInfo<Date> COL_CREATED_ON =
    new IssueColumnInfo.IssueDateColumnInfo(RevuBundle.message("browsing.items.table.createdOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getCreatedOn();
      }
    };

  private static final IssueColumnInfo<String> COL_LAST_UPDATED_BY =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.items.table.lastUpdatedBy.title"),
      IssueColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getLastUpdatedBy().getDisplayName();
      }
    };

  private static final IssueColumnInfo<Date> COL_LAST_UPDATED_ON =
    new IssueColumnInfo.IssueDateColumnInfo(RevuBundle.message("browsing.items.table.lastUpdatedOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getLastUpdatedOn();
      }
    };

  private static final IssueColumnInfo<String> COL_PRIORITY = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.items.table.priority.title"), IssueColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return (issue.getPriority() == null) ? "" : issue.getPriority().getName();
    }
  };

  private static final IssueColumnInfo<String> COL_STATUS = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.items.table.status.title"), IssueColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return RevuUtils.buildStatusLabel(issue.getStatus());
    }
  };

  private static final IssueColumnInfo<String> COL_TAGS = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.items.table.tags.title"), IssueColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return ConverterUtils.toString(issue.getTags(), false);
    }
  };

  private static final IssueColumnInfo<String> COL_TITLE =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.items.table.title.title"),
      IssueColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(Issue issue)
      {
        return issue.getSummary();
      }

      @Override
      public TableCellRenderer getRenderer(final Issue issue)
      {
        return new DefaultTableCellRenderer()
        {
          @Override
          public String getToolTipText()
          {
            return (issue == null) ? null : issue.getDesc();
          }
        };
      }
    };

  private static final IssueColumnInfo<String> COL_VCS_REV =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.items.table.vcsRev.title"),
      IssueColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(Issue issue)
      {
        return issue.getVcsRev();
      }
    };

  public static final IssueColumnInfo[] DEFAULT_COLUMN_INFOS =
    {
      COL_PRIORITY,
      COL_STATUS,
      COL_TITLE,
      COL_TAGS,
      COL_CREATED_BY,
      COL_CREATED_ON
    };

  public static final IssueColumnInfo[] ALL_COLUMN_INFOS =
    {
      COL_PRIORITY,
      COL_STATUS,
      COL_VCS_REV,
      COL_TITLE,
      COL_TAGS,
      COL_CREATED_BY,
      COL_CREATED_ON,
      COL_LAST_UPDATED_BY,
      COL_LAST_UPDATED_ON,
    };

  public static final Map<String, IssueColumnInfo> ALL_COLUMN_INFOS_BY_NAMES
    = new LinkedHashMap<String, IssueColumnInfo>();
  static
  {
    for (IssueColumnInfo columnInfo : ALL_COLUMN_INFOS)
    {
      ALL_COLUMN_INFOS_BY_NAMES.put(columnInfo.getName(), columnInfo);
    }
  }
}
