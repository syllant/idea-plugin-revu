package org.sylfra.idea.plugins.revu.settings.project.workspace;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

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

  public RevuWorkspaceSettings()
  {
    reviewFiles = new ArrayList<String>();
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
}