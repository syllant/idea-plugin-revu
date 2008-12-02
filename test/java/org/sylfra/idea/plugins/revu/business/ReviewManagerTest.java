package org.sylfra.idea.plugins.revu.business;

import com.intellij.testFramework.IdeaTestCase;
import org.junit.Assert;
import org.sylfra.idea.plugins.revu.RevuException;
import org.sylfra.idea.plugins.revu.model.Review;

/**
 * @author <a href="mailto:sylvain.francois@kalistick.fr">Sylvain FRANCOIS</a>
 * @version $Id$
 */
public class ReviewManagerTest extends IdeaTestCase
{
  public void testCheckCyclicLink()
  {
    ReviewManager reviewManager = new ReviewManager(getProject());

    Review review1 = new Review("review1");
    Review review2 = new Review("review2");
    Review review3 = new Review("review3");

    try
    {
      reviewManager.checkCyclicLink(review1, review1);
    }
    catch (RevuException e)
    {
      Assert.fail(e.getMessage());
    }

    review2.setExtendedReview(review1);
    try
    {
      reviewManager.checkCyclicLink(review1, review2);
    }
    catch (RevuException e)
    {
      Assert.fail(e.getMessage());
    }

    review2.setExtendedReview(review3);
    review3.setExtendedReview(review1);
    try
    {
      reviewManager.checkCyclicLink(review1, review2);
    }
    catch (RevuException e)
    {
      Assert.fail(e.getMessage());
    }
    try
    {
      reviewManager.checkCyclicLink(review1, review3);
    }
    catch (RevuException e)
    {
      Assert.fail(e.getMessage());
    }
  }

}
