package org.sylfra.idea.plugins.revu.settings.project.workspace;

import org.jetbrains.annotations.Nullable;
import org.sylfra.idea.plugins.revu.settings.project.AbstractReviewFilesRevuSettings;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuWorkspaceSettings extends AbstractReviewFilesRevuSettings<RevuWorkspaceSettings>
{
  private boolean autoScrollToSource;
  private boolean filterFilesWithIssues;
  private String reviewingReviewName;
  private String lastSelectedReviewDir;

  public RevuWorkspaceSettings()
  {
    filterFilesWithIssues = false;
  }

  public boolean isAutoScrollToSource()
  {
    return autoScrollToSource;
  }

  public void setAutoScrollToSource(boolean autoScrollToSource)
  {
    this.autoScrollToSource = autoScrollToSource;
  }

  public boolean isFilterFilesWithIssues()
  {
    return filterFilesWithIssues;
  }

  public void setFilterFilesWithIssues(boolean filterFilesWithIssues)
  {
    this.filterFilesWithIssues = filterFilesWithIssues;
  }

  public String getLastSelectedReviewDir()
  {
    return lastSelectedReviewDir;
  }

  public void setLastSelectedReviewDir(String lastSelectedReviewDir)
  {
    this.lastSelectedReviewDir = lastSelectedReviewDir;
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