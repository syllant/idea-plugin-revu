package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Review;

import java.io.File;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewExternalizationListener
{
  void loadFailed(File file, Exception exception);

  void loadSucceeded(Review review);

  void saveFailed(Review review, Exception exception);

  void saveSucceeded(Review review);
}