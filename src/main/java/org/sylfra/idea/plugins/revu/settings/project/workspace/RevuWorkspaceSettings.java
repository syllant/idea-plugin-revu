package org.sylfra.idea.plugins.revu.settings.project.workspace;

import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.model.Filter;
import org.sylfra.idea.plugins.revu.settings.project.AbstractReviewFilesRevuSettings;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueColumnInfo;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueColumnInfoRegistry;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuWorkspaceSettings extends AbstractReviewFilesRevuSettings
{
  private boolean autoScrollToSource;
  // Saved as String because IDEA don't store settings if attribute is a int with 0 value
  private String toolWindowSplitOrientation;
  private List<String> browsingColNames;
  private List<Filter> filters;
  private int selectedFilterIndex;
  private boolean filterFilesWithIssues;
  private String reviewingReviewName;

  public RevuWorkspaceSettings()
  {
    filters = new ArrayList<Filter>();
    selectedFilterIndex = -1;
    toolWindowSplitOrientation = String.valueOf(JSplitPane.HORIZONTAL_SPLIT);
    filterFilesWithIssues = false;

    IssueColumnInfo[] defaultColumnInfos = IssueColumnInfoRegistry.DEFAULT_COLUMN_INFOS;
    browsingColNames = new ArrayList<String>(defaultColumnInfos.length);
    for (IssueColumnInfo columnInfo : defaultColumnInfos)
    {
      browsingColNames.add(columnInfo.getName());
    }
  }

  public boolean isAutoScrollToSource()
  {
    return autoScrollToSource;
  }

  public void setAutoScrollToSource(boolean autoScrollToSource)
  {
    this.autoScrollToSource = autoScrollToSource;
  }

  public String getToolWindowSplitOrientation()
  {
    return toolWindowSplitOrientation;
  }

  public void setToolWindowSplitOrientation(String toolWindowSplitOrientation)
  {
    this.toolWindowSplitOrientation = toolWindowSplitOrientation;
  }

  public List<String> getBrowsingColNames()
  {
    return browsingColNames;
  }

  public void setBrowsingColNames(List<String> browsingColNames)
  {
    this.browsingColNames = browsingColNames;
  }

  public List<Filter> getFilters()
  {
    return filters;
  }

  public void setFilters(List<Filter> filters)
  {
    this.filters = filters;
  }

  public Filter getSelectedFilter()
  {
    return ((selectedFilterIndex < 0) || (selectedFilterIndex >= filters.size()))
      ? null : filters.get(selectedFilterIndex);
  }

  public void setSelectedFilter(@Nullable Filter filter)
  {
    selectedFilterIndex = filters.indexOf(filter);
  }

  public boolean isFilterFilesWithIssues()
  {
    return filterFilesWithIssues;
  }

  public void setFilterFilesWithIssues(boolean filterFilesWithIssues)
  {
    this.filterFilesWithIssues = filterFilesWithIssues;
  }

  @Nullable
  public String getReviewingReviewName()
  {
    return reviewingReviewName;
  }

  public void setReviewingReviewName(@Nullable String reviewingReviewName)
  {
    this.reviewingReviewName = reviewingReviewName;
  }
}