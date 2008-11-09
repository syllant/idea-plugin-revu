package org.sylfra.idea.plugins.revu.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuSettings
{
  private boolean autoScrollToSource;
  private List<String> reviewFiles;

  public RevuSettings()
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

  public List<String> getReviewFiles()
  {
    return reviewFiles;
  }

  public void setReviewFiles(List<String> reviewFiles)
  {
    this.reviewFiles = reviewFiles;
  }
}
