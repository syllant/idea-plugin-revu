package org.sylfra.idea.plugins.revu.ui.browsingtable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.RevuBundle;
import org.sylfra.idea.plugins.revu.config.impl.ConverterUtils;
import org.sylfra.idea.plugins.revu.model.ReviewItem;
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
public class ReviewItemColumnInfoRegistry
{
  private static final ReviewItemColumnInfo<String> COL_CREATED_BY =
    new ReviewItemColumnInfo<String>(RevuBundle.message("browsing.items.table.createdBy.title"),
      ReviewItemColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(@NotNull ReviewItem reviewItem)
      {
        return reviewItem.getHistory().getCreatedBy().getDisplayName();
      }
    };

  private static final ReviewItemColumnInfo<Date> COL_CREATED_ON =
    new ReviewItemColumnInfo.ReviewItemDateColumnInfo(RevuBundle.message("browsing.items.table.createdOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull ReviewItem reviewItem)
      {
        return reviewItem.getHistory().getCreatedOn();
      }
    };

  private static final ReviewItemColumnInfo<String> COL_LAST_UPDATED_BY =
    new ReviewItemColumnInfo<String>(RevuBundle.message("browsing.items.table.lastUpdatedBy.title"),
      ReviewItemColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(@NotNull ReviewItem reviewItem)
      {
        return reviewItem.getHistory().getLastUpdatedBy().getDisplayName();
      }
    };

  private static final ReviewItemColumnInfo<Date> COL_LAST_UPDATED_ON =
    new ReviewItemColumnInfo.ReviewItemDateColumnInfo(RevuBundle.message("browsing.items.table.lastUpdatedOn.title"))
    {
      @Nullable
      public Date valueOf(@NotNull ReviewItem reviewItem)
      {
        return reviewItem.getHistory().getLastUpdatedOn();
      }
    };

  private static final ReviewItemColumnInfo<String> COL_PRIORITY = new ReviewItemColumnInfo<String>(
    RevuBundle.message("browsing.items.table.priority.title"), ReviewItemColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(ReviewItem reviewItem)
    {
      return (reviewItem.getPriority() == null) ? "" : reviewItem.getPriority().getName();
    }
  };

  private static final ReviewItemColumnInfo<String> COL_STATUS = new ReviewItemColumnInfo<String>(
    RevuBundle.message("browsing.items.table.status.title"), ReviewItemColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(ReviewItem reviewItem)
    {
      return RevuUtils.buildStatusLabel(reviewItem.getResolutionStatus());
    }
  };

  private static final ReviewItemColumnInfo<String> COL_TAGS = new ReviewItemColumnInfo<String>(
    RevuBundle.message("browsing.items.table.tags.title"), ReviewItemColumnInfo.FilterType.CONTAINS)
  {
    @Nullable
    public String valueOf(ReviewItem reviewItem)
    {
      return ConverterUtils.toString(reviewItem.getTags(), false);
    }
  };

  private static final ReviewItemColumnInfo<String> COL_TITLE =
    new ReviewItemColumnInfo<String>(RevuBundle.message("browsing.items.table.title.title"),
      ReviewItemColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
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
    };

  private static final ReviewItemColumnInfo<String> COL_VCS_REV =
    new ReviewItemColumnInfo<String>(RevuBundle.message("browsing.items.table.vcsRev.title"),
      ReviewItemColumnInfo.FilterType.CONTAINS)
    {
      @Nullable
      public String valueOf(ReviewItem reviewItem)
      {
        return reviewItem.getVcsRev();
      }
    };

  public static final ReviewItemColumnInfo[] DEFAULT_COLUMN_INFOS =
    {
      COL_PRIORITY,
      COL_STATUS,
      COL_TITLE,
      COL_TAGS,
      COL_CREATED_BY,
      COL_CREATED_ON
    };

  public static final ReviewItemColumnInfo[] ALL_COLUMN_INFOS =
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

  public static final Map<String, ReviewItemColumnInfo> ALL_COLUMN_INFOS_BY_NAMES 
    = new LinkedHashMap<String, ReviewItemColumnInfo>();
  static
  {
    for (ReviewItemColumnInfo columnInfo : ALL_COLUMN_INFOS)
    {
      ALL_COLUMN_INFOS_BY_NAMES.put(columnInfo.getName(), columnInfo);
    }
  }
}
