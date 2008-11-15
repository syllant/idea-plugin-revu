package org.sylfra.idea.plugins.revu.settings.project;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * General settings bean for plugin
 *
 * @author <a href="mailto:sylfradev@yahoo.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class RevuProjectSettings implements IRevuSettings
{
  private List<String> reviewFiles;

  public RevuProjectSettings()
  {
    reviewFiles = new ArrayList<String>();
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
