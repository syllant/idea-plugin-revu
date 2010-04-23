package org.sylfra.idea.plugins.revu.settings.project;

import org.sylfra.idea.plugins.revu.settings.IRevuSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class AbstractReviewFilesRevuSettings<T extends AbstractReviewFilesRevuSettings> implements IRevuSettings<T>
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

  @Override
  public T clone()
  {
    T result = null;
    try
    {
      result = (T) super.clone();
      result.reviewFiles = new ArrayList<String>(reviewFiles);
    }
    catch (CloneNotSupportedException ignored)
    {
    }

    return result;
  }
}
