package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Review;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewExternalizationAdapter implements IReviewExternalizationListener
{
  public void loadFailed(String path, Exception exception)
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
