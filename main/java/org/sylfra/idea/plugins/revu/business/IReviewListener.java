package org.sylfra.idea.plugins.revu.business;

import org.sylfra.idea.plugins.revu.model.Review;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public interface IReviewListener
{
  void reviewAdded(Review review);

  void reviewDeleted(Review review);
}
