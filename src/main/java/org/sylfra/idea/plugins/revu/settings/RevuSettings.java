package org.sylfra.idea.plugins.revu.settings;

import org.sylfra.idea.plugins.revu.model.Review;

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
  private List<Review> reviews;

  public RevuSettings()
  {
    reviews = new ArrayList<Review>();
  }

  public List<Review> getReviews()
  {
    return reviews;
  }

  public void setReviews(List<Review> reviews)
  {
    this.reviews = reviews;
  }
}
