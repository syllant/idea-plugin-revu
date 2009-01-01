package org.sylfra.idea.plugins.revu.settings.project;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class AbstractReviewFilesRevuSettings implements IRevuSettings
{
  protected List<String> reviewFiles;

  public AbstractReviewFilesRevuSettings()
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
