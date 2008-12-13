package org.sylfra.idea.plugins.revu.settings.project.workspace;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueColumnInfo;
import org.sylfra.idea.plugins.revu.ui.browsingtable.IssueColumnInfoRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuWorkspaceSettings implements IRevuSettings
{
  private boolean autoScrollToSource;
  // Saved as String because IDEA don't store settings if attribute is a int with 0 value
  private String toolWindowSplitOrientation;
  private List<String> reviewFiles;
  private List<String> browsingColNames;

  public RevuWorkspaceSettings()
  {
    reviewFiles = new ArrayList<String>();

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

  public List<String> getReviewFiles()
  {
    return reviewFiles;
  }

  public void setReviewFiles(List<String> reviewFiles)
  {
    this.reviewFiles = reviewFiles;
  }

  public List<String> getBrowsingColNames()
  {
    return browsingColNames;
  }

  public void setBrowsingColNames(List<String> browsingColNames)
  {
    this.browsingColNames = browsingColNames;
  }
}