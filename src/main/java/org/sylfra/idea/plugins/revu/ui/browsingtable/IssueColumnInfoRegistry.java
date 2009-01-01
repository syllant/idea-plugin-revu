package org.sylfra.idea.plugins.revu.ui.browsingtable;

import com.intellij.util.ui.ColumnInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.externalizing.impl.ConverterUtils;
import org.sylfra.idea.plugins.revu.model.Issue;
import org.sylfra.idea.plugins.revu.utils.RevuUtils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author <a href="mailto:sylfrade@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class IssueColumnInfoRegistry
{
  private static final IssueColumnInfo<String> COL_CREATED_BY =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.issues.table.createdBy.title"),
      IssueColumnInfo.FilterType.CONTAINS, 80, -1, 100)
    {
      @Nullable
      public String valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getCreatedBy().getDisplayName();
      }
    };

  private static final IssueColumnInfo.IssueDateColumnInfo<Date> COL_CREATED_ON =
    new IssueColumnInfo.IssueDateColumnInfo<Date>(RevuBundle.message("browsing.issues.table.createdOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getCreatedOn();
      }
    };

  private static final IssueColumnInfo<String> COL_LAST_UPDATED_BY =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.issues.table.lastUpdatedBy.title"),
      IssueColumnInfo.FilterType.CONTAINS, 80, -1, 100)
    {
      @Nullable
      public String valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getLastUpdatedBy().getDisplayName();
      }
    };

  private static final IssueColumnInfo.IssueDateColumnInfo<Date> COL_LAST_UPDATED_ON =
    new IssueColumnInfo.IssueDateColumnInfo<Date>(RevuBundle.message("browsing.issues.table.lastUpdatedOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull Issue issue)
      {
        return issue.getHistory().getLastUpdatedOn();
      }
    };

  private static final IssueColumnInfo<Integer> COL_NOTES_COUNT = new IssueColumnInfo<Integer>(
    RevuBundle.message("browsing.issues.table.notesCount.title"), IssueColumnInfo.FilterType.CONTAINS, 50, 50, 50)
  {
    @Nullable
    public Integer valueOf(Issue issue)
    {
      return issue.getNotes().size();
    }
  };

  private static final IssueColumnInfo<String> COL_PRIORITY = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.issues.table.priority.title"), IssueColumnInfo.FilterType.CONTAINS, 60, 30, 70)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return (issue.getPriority() == null) ? "" : issue.getPriority().getName();
    }
  };

  private static final IssueColumnInfo<String> COL_REVIEW = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.issues.table.review.title"), IssueColumnInfo.FilterType.CONTAINS, 100, -1, 100)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return issue.getReview().getName();
    }
  };

  private static final IssueColumnInfo<String> COL_STATUS = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.issues.table.status.title"), IssueColumnInfo.FilterType.CONTAINS, 80, 50, 100)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return RevuUtils.buildIssueStatusLabel(issue.getStatus());
    }
  };

  private static final IssueColumnInfo<String> COL_SUMMARY =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.issues.table.summary.title"),
      IssueColumnInfo.FilterType.CONTAINS, -1, 50, -1)
    {
      @Nullable
      public String valueOf(Issue issue)
      {
        return issue.getSummary();
      }

      @Override
      protected void customizeRenderer(JLabel result, String value, boolean selected, boolean hasFocus)
      {
        if (selected)
        {
          result.setFont(result.getFont().deriveFont(Font.BOLD));
        }
      }

      @Override
      protected String getToolTipText(Issue issue)
      {
        return (issue == null)
          ? null
          : (((issue.getDesc() != null) && (issue.getDesc().length() > 0))
            ? issue.getDesc()
            : super.getToolTipText(issue));
      }
    };

  private static final IssueColumnInfo<String> COL_TAGS = new IssueColumnInfo<String>(
    RevuBundle.message("browsing.issues.table.tags.title"), IssueColumnInfo.FilterType.CONTAINS, 80, 40, 200)
  {
    @Nullable
    public String valueOf(Issue issue)
    {
      return ConverterUtils.toString(issue.getTags(), false);
    }
  };

  private static final IssueColumnInfo<String> COL_VCS_REV =
    new IssueColumnInfo<String>(RevuBundle.message("browsing.issues.table.vcsRev.title"),
      IssueColumnInfo.FilterType.CONTAINS, 50, 20, 100)
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
      COL_SUMMARY,
      COL_TAGS,
      COL_CREATED_BY,
      COL_CREATED_ON
    };

  public static final IssueColumnInfo[] ALL_COLUMN_INFOS =
    {
      COL_REVIEW,
      COL_PRIORITY,
      COL_STATUS,
      COL_VCS_REV,
      COL_SUMMARY,
      COL_TAGS,
      COL_NOTES_COUNT,
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

  public static List<String> getColumnNames(List<? extends ColumnInfo> columnInfos)
  {
    List<String> colNames = new ArrayList<String>(columnInfos.size());
    for (ColumnInfo columnInfo : columnInfos)
    {
      colNames.add(columnInfo.getName());
    }

    return colNames;
  }
}
