package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Review;

import java.io.File;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizationAdapter implements IReviewExternalizationListener
{
  public void loadFailed(File file, Exception exception)
  {
  }

  public void loadSucceeded(Review review)
  {
  }

  public void saveFailed(Review review, Exception exception)
  {
  }

  public void saveSucceeded(Review review)
  {
  }
}
