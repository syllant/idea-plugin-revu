package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Review;

/**
 * @author <a href="mailto:syllant@gmail.com">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewListener
{
  void reviewChanged(Review review);

  void reviewAdded(Review review);

  void reviewDeleted(Review review);
}
